package com.strataurban.strata.yoco_integration.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.OfferStatus;
import com.strataurban.strata.Notifications.NotificationFacade;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.ServiceImpls.v2.ReceiptService;
import com.strataurban.strata.yoco_integration.config.YocoProperties;
import com.strataurban.strata.yoco_integration.dtos.WebhookPayload;
import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import com.strataurban.strata.yoco_integration.entities.WebhookEvent;
import com.strataurban.strata.yoco_integration.exceptions.WebhookValidationException;
import com.strataurban.strata.yoco_integration.repositories.PaymentTransactionRepository;
import com.strataurban.strata.yoco_integration.repositories.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static com.strataurban.strata.Enums.OfferStatus.PAID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookEventRepository webhookEventRepository;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final YocoProperties yocoProperties;
    private final ObjectMapper objectMapper;

    private static final String HMAC_SHA256 = "HmacSHA256";
    private final BookingRepository bookingRepository;
    private final NotificationFacade notificationFacade;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OfferRepository offerRepository;
    private final ReceiptService receiptService;

    /**
     * Processes incoming webhook from YOCO
     * CRITICAL: This method must respond quickly (within 5 seconds)
     */
    @Transactional
    public void processWebhook(String rawPayload, Map<String, String> headers) {

        long startTime = System.currentTimeMillis();
        String eventId = headers.get("webhook-id");

        log.info("Processing webhook event: {}", eventId);

        try {
            // 1. Validate webhook signature (CRITICAL FOR SECURITY)
            validateWebhookSignature(rawPayload, headers);

            // 2. Check for duplicate webhook (idempotency)
            if (isDuplicateWebhook(eventId)) {
                log.info("Duplicate webhook detected and ignored: {}", eventId);
                return;
            }

            // 3. Parse webhook payload
            WebhookPayload payload = objectMapper.readValue(rawPayload, WebhookPayload.class);


            Offer offer = offerRepository.findByTransactionReference((String) payload.getPayload().getMetadata().get("externalReference"));
            if (!ObjectUtils.isEmpty(offer) || offer!= null) {
                offer.setStatus(PAID);
                offerRepository.save(offer);
            }
            // 4. Store raw webhook for audit trail
            WebhookEvent webhookEvent = storeWebhookEvent(eventId, rawPayload, payload, true);

            // 5. Process webhook based on event type
            processWebhookEvent(payload, webhookEvent);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Webhook processed successfully in {}ms: EventId={}, Type={}",
                    duration, eventId, payload.getType());

        } catch (WebhookValidationException e) {
            log.error("Webhook validation failed: EventId={}, Error={}", eventId, e.getMessage());
            storeFailedWebhook(eventId, rawPayload, e.getMessage(), false);
            throw e;

        } catch (Exception e) {
            log.error("Error processing webhook: EventId={}", eventId, e);
            storeFailedWebhook(eventId, rawPayload, e.getMessage(), true);
            throw new WebhookValidationException("Webhook processing failed", e);
        }
    }

    /**
     * Validates webhook signature to ensure it's from YOCO
     * CRITICAL FOR SECURITY: Prevents webhook spoofing
     */
    private void validateWebhookSignature(String payload, Map<String, String> headers) {

        if (!yocoProperties.getWebhook().isSignatureValidationEnabled()) {
            log.warn("Webhook signature validation is DISABLED - this is insecure!");
            return;
        }

        String webhookId = headers.get("webhook-id");
        String webhookTimestamp = headers.get("webhook-timestamp");
        String webhookSignature = headers.get("webhook-signature");

        if (webhookId == null || webhookTimestamp == null || webhookSignature == null) {
            throw new WebhookValidationException("Missing required webhook headers");
        }

        // Validate timestamp to prevent replay attacks
        validateWebhookTimestamp(webhookTimestamp);

        // Construct signed content: {id}.{timestamp}.{payload}
        String signedContent = String.format("%s.%s.%s", webhookId, webhookTimestamp, payload);

        // Calculate expected signature
        String expectedSignature = calculateHmacSignature(signedContent);

        // Extract signature from header (format: "v1,signature")
        String receivedSignature = extractSignature(webhookSignature);

        // Compare signatures using constant-time comparison to prevent timing attacks
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new WebhookValidationException("Invalid webhook signature");
        }

        log.debug("Webhook signature validated successfully");
    }

    /**
     * Validates webhook timestamp to prevent replay attacks
     */
    private void validateWebhookTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTimestamp = Instant.now().getEpochSecond();
            long difference = Math.abs(currentTimestamp - timestamp);

            int toleranceSeconds = yocoProperties.getWebhook().getTimestampToleranceSeconds();

            if (difference > toleranceSeconds) {
                throw new WebhookValidationException(
                        String.format("Webhook timestamp too old or in future. Difference: %d seconds", difference)
                );
            }
        } catch (NumberFormatException e) {
            throw new WebhookValidationException("Invalid webhook timestamp format");
        }
    }

    /**
     * Calculates HMAC SHA256 signature
     */
    private String calculateHmacSignature(String data) {
        try {
            String webhookSecret = yocoProperties.getApi().getWebhookSecret();

            // Remove 'whsec_' prefix and decode base64
            String secretKey = webhookSecret.substring(6);
            byte[] secretBytes = Base64.getDecoder().decode(secretKey);

            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretBytes, HMAC_SHA256);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);

        } catch (Exception e) {
            log.error("Error calculating HMAC signature", e);
            throw new WebhookValidationException("Signature calculation failed", e);
        }
    }

    /**
     * Extracts signature from webhook-signature header
     */
    private String extractSignature(String signatureHeader) {
        // Format: "v1,base64signature" or just "base64signature"
        String[] parts = signatureHeader.split(",");
        return parts.length > 1 ? parts[1] : parts[0];
    }

    /**
     * Checks if webhook has already been processed (idempotency)
     */
    private boolean isDuplicateWebhook(String eventId) {
        return webhookEventRepository.existsByEventId(eventId);
    }

    /**
     * Processes webhook based on event type
     */
    private void processWebhookEvent(WebhookPayload payload, WebhookEvent webhookEvent) {

        String eventType = payload.getType();
        String checkoutId = extractCheckoutId(payload);

        switch (eventType) {
            case "payment.succeeded":
                handlePaymentSucceeded(checkoutId, payload);
                break;

            case "payment.failed":
                handlePaymentFailed(checkoutId, payload);
                break;

            case "refund.succeeded":
                handleRefundSucceeded(payload);
                break;

            case "refund.failed":
                handleRefundFailed(payload);
                break;

            default:
                log.warn("Unknown webhook event type: {}", eventType);
        }

        // Mark webhook as processed
        webhookEvent.setProcessed(true);
        webhookEvent.setProcessedAt(LocalDateTime.now());
        webhookEventRepository.save(webhookEvent);
    }
//
//    private void handlePaymentSucceeded(String checkoutId, WebhookPayload payload) {
//        String paymentId = payload.getPayload().getPaymentId();
//
//
//        paymentService.updatePaymentStatus(
//                checkoutId,
//                PaymentTransaction.PaymentStatus.SUCCEEDED,
//                paymentId,
//                null
//        );
//
//        log.info("Payment succeeded: CheckoutId={}, PaymentId={}", checkoutId, paymentId);
//
//        // TODO: Trigger order fulfillment, send confirmation email, etc.
//    }



    private void handlePaymentSucceeded(String checkoutId, WebhookPayload payload) {
        String paymentId = payload.getPayload().getId();

        // Update payment status in database
        paymentService.updatePaymentStatus(
                checkoutId,
                PaymentTransaction.PaymentStatus.SUCCEEDED,
                paymentId,
                null
        );

        log.info("Payment succeeded: CheckoutId={}, PaymentId={}", checkoutId, paymentId);

        try {
            // Retrieve payment transaction details
            PaymentTransaction payment = paymentService.getPaymentByCheckoutId(checkoutId);

            if (payment == null) {
                log.error("Payment transaction not found for checkoutId: {}", checkoutId);
                return;
            }

            Long bookingId = payment.getBookingId();
            Long clientId = payment.getCustomerId();
            Long providerId = payment.getRecipientId();
            String amount = payment.getAmount().toString();
            String bookingReference = payment.getExternalReference();

            // Send notifications to both client and provider
            notificationFacade.notifyPaymentSuccessful(
                    clientId,
                    providerId,
                    bookingId,
                    amount,
                    bookingReference
            );

            // Order fulfillment: Update booking status and trigger next steps
            fulfillBookingOrder(bookingId, providerId, payment.getId());

            log.info("Payment processing completed for booking: {}", bookingId);

        } catch (Exception e) {
            log.error("Error processing payment success for checkoutId {}: {}",
                    checkoutId, e.getMessage(), e);
        }
    }

    /**
     * Order fulfillment - Complete the booking process after successful payment
     * This includes:
     * - Update booking status to CONFIRMED/PAID
     * - Assign driver (if available)
     * - Update provider's schedule
     * - Generate receipt
     */
    private void fulfillBookingOrder(Long bookingId, Long providerId, Long paymentId) {
        try {
            log.info("Starting order fulfillment for booking: {}", bookingId);

            // 1. Update booking status to CONFIRMED
            BookingRequest booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Booking {} status updated to CONFIRMED", bookingId);

            // 2. TODO: Assign driver if auto-assignment is enabled
            // driverService.assignDriverToBooking(bookingId, providerId);

            // 3. TODO: Update provider's schedule/availability
            // providerScheduleService.blockSchedule(providerId, booking.getServiceDate());

            // 4. TODO: Generate receipt
             receiptService.generateReceipt(paymentId, 4L);
            // 5. TODO: Send confirmation email with booking details
            // emailService.sendBookingConfirmationEmail(booking);

            log.info("Order fulfillment completed for booking: {}", bookingId);

        } catch (Exception e) {
            log.error("Error in order fulfillment for booking {}: {}", bookingId, e.getMessage(), e);
            // Don't throw - payment already succeeded, fulfillment can be retried
        }
    }

    private void handlePaymentFailed(String checkoutId, WebhookPayload payload) {
        paymentService.updatePaymentStatus(
                checkoutId,
                PaymentTransaction.PaymentStatus.FAILED,
                null,
                "Payment failed"
        );

        log.warn("Payment failed: CheckoutId={}", checkoutId);

        // TODO: Notify customer, update order status, etc.
    }

    private void handleRefundSucceeded(WebhookPayload payload) {
        String refundId = payload.getPayload().getId();
        refundService.updateRefundStatus(refundId, true, null);

        log.info("Refund succeeded: RefundId={}", refundId);
    }

    private void handleRefundFailed(WebhookPayload payload) {
        String refundId = payload.getPayload().getId();
        refundService.updateRefundStatus(refundId, false, "Refund failed");

        log.warn("Refund failed: RefundId={}", refundId);
    }

    private String extractCheckoutId(WebhookPayload payload) {
        Map<String, Object> metadata = payload.getPayload().getMetadata();
        if (metadata != null && metadata.containsKey("checkoutId")) {
            return metadata.get("checkoutId").toString();
        }
        throw new WebhookValidationException("CheckoutId not found in webhook metadata");
    }

    private WebhookEvent storeWebhookEvent(
            String eventId,
            String rawPayload,
            WebhookPayload payload,
            boolean signatureValid) {

        return webhookEventRepository.save(
                WebhookEvent.builder()
                        .eventId(eventId)
                        .checkoutId(extractCheckoutId(payload))
                        .eventType(payload.getType())
                        .rawPayload(rawPayload)
                        .processed(false)
                        .signatureValid(signatureValid)
                        .retryCount(0)
                        .build()
        );
    }

    private void storeFailedWebhook(
            String eventId,
            String rawPayload,
            String error,
            boolean signatureValid) {

        try {
            webhookEventRepository.save(
                    WebhookEvent.builder()
                            .eventId(eventId)
                            .checkoutId("UNKNOWN")
                            .eventType("UNKNOWN")
                            .rawPayload(rawPayload)
                            .processed(false)
                            .signatureValid(signatureValid)
                            .processingError(error)
                            .retryCount(0)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to store failed webhook", e);
        }
    }
}