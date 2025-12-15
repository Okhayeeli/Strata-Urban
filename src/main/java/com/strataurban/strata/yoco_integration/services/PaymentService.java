package com.strataurban.strata.yoco_integration.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.yoco_integration.config.YocoProperties;
import com.strataurban.strata.yoco_integration.dtos.CheckoutResponse;
import com.strataurban.strata.yoco_integration.dtos.CreateCheckoutRequest;
import com.strataurban.strata.yoco_integration.dtos.InitiatePaymentRequest;
import com.strataurban.strata.yoco_integration.dtos.PaymentResponse;
import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import com.strataurban.strata.yoco_integration.exceptions.PaymentException;
import com.strataurban.strata.yoco_integration.repositories.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RestTemplate yocoRestTemplate;
    private final PaymentTransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;
    private final YocoProperties yocoProperties;
    private final ObjectMapper objectMapper;
    private final TransactionValidationService transactionValidationService;
    private final OfferRepository offerRepository;


    @Value("${yoco.payment.success.url}")
    private String successUrl;

    @Value("${yoco.payment.cancel.url}")
    private String cancelUrl;

    @Value("${yoco.payment.failure.url}")
    private String failureUrl;

    /**
     * Initiates a payment with YOCO
     * Implements idempotency to prevent duplicate charges
     */
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, SecurityUserDetails userDetails) {

        request.setCustomerId(userDetails.getId());
        Offer offer = offerRepository.findByTransactionReference(request.getExternalReference());
        request.setRecipientId(offer.getProviderId());
        request.setBookingId(offer.getBookingRequestId());
        log.info("Initiating payment for externalRef: {}, customerId: {}, amount: {}",
                request.getExternalReference(),
                request.getCustomerId(),
                request.getAmount());
        // Generate idempotency key for this payment (same for 2 minutes for the same externalReference)
        String idempotencyKey = generateIdempotencyKey(request.getExternalReference());

        // Check if a payment already exists with this idempotency key
        Optional<PaymentTransaction> existingPayment = idempotencyService.findByIdempotencyKey(idempotencyKey);

        if (existingPayment.isPresent()) {
            log.warn("Duplicate payment attempt detected for idempotency key: {}", idempotencyKey);
            throw new PaymentException(
                    "A payment with this reference was already initiated. " +
                            "Please wait 2 minutes before retrying to avoid duplicate transactions."
            );
        }

        // Validate request
        validatePaymentRequest(request);

        // Create checkout with YOCO
        CreateCheckoutRequest checkoutRequest = buildCheckoutRequest(request);
        CheckoutResponse checkoutResponse = createCheckoutWithYoco(checkoutRequest, idempotencyKey);

        // Store transaction in database
        PaymentTransaction transaction = saveTransaction(request, checkoutResponse, idempotencyKey);

        log.info("Payment initiated successfully. CheckoutId: {}, ExternalRef: {}",
                checkoutResponse.getId(), request.getExternalReference());

        return mapToPaymentResponse(transaction);
    }

    /**
     * Creates checkout with YOCO API with idempotency support
     */
    private CheckoutResponse createCheckoutWithYoco(
            CreateCheckoutRequest request,
            String idempotencyKey) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(yocoProperties.getApi().getSecretKey());
            headers.set("Idempotency-Key", idempotencyKey);

            HttpEntity<CreateCheckoutRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<CheckoutResponse> response = yocoRestTemplate.exchange(
                    "/checkouts",
                    HttpMethod.POST,
                    entity,
                    CheckoutResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Checkout created successfully: {}", response.getBody().getId());
                return response.getBody();
            }

            throw new PaymentException("Failed to create checkout with YOCO");

        } catch (HttpClientErrorException e) {
            log.error("YOCO API error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException("Payment provider error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating checkout", e);
            throw new PaymentException("Failed to initiate payment", e);
        }
    }

    /**
     * Retrieves payment status from database
     */
    @Transactional(readOnly = true)
    public PaymentTransaction getPaymentByCheckoutId(String checkoutId) {
        return transactionRepository.findByCheckoutId(checkoutId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + checkoutId));
    }

    /**
     * Retrieves payment by external reference (your order ID)
     */
    @Transactional(readOnly = true)
    public PaymentTransaction getPaymentByExternalReference(String externalReference) {
        return transactionRepository.findByExternalReference(externalReference)
                .orElseThrow(() -> new PaymentException("Payment not found for reference: " + externalReference));
    }

    /**
     * Updates payment status (called by webhook handler)
     */
    @Transactional
    public void updatePaymentStatus(String checkoutId, PaymentTransaction.PaymentStatus status,
                                    String paymentId, String errorMessage) {

        PaymentTransaction transaction = getPaymentByCheckoutId(checkoutId);

        transaction.setStatus(status);
        transaction.setPaymentId(paymentId);

        if (errorMessage != null) {
            transaction.setErrorMessage(errorMessage);
        }

        if (status == PaymentTransaction.PaymentStatus.SUCCEEDED ||
                status == PaymentTransaction.PaymentStatus.FAILED) {
            transaction.setCompletedAt(LocalDateTime.now());
        }

        transactionRepository.save(transaction);

        log.info("Payment status updated: CheckoutId={}, Status={}", checkoutId, status);
    }

    // Helper methods

    private void validatePaymentRequest(InitiatePaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Invalid amount");
        }

        if (request.getAmount().compareTo(new BigDecimal("0.02")) < 0) {
            throw new PaymentException("Minimum payment amount is R2.00");
        }

        if (request.getExternalReference() == null || request.getExternalReference().isBlank()) {
            throw new PaymentException("External reference is required");
        }

        if (!transactionValidationService.transactionExists(request.getExternalReference())){
            throw new PaymentException("Transaction not found");
        }

        if (request.getCustomerId() == null || request.getCustomerId() <= 0) {
            throw new PaymentException("Customer ID is required and must be a positive number");
        }

        if (!transactionValidationService.customerExists(request.getCustomerId())){
            throw new PaymentException("Invalid Customer Id: " + request.getCustomerId());
        }

        if (!transactionValidationService.isCorrectOfferAmount(request.getAmount(), request.getExternalReference())){
            throw new PaymentException("Invalid transaction amount");
        }

        if(!transactionValidationService.isCorrectCurrency(request.getCurrency(), request.getExternalReference())){
            throw new PaymentException("Invalid currency");
        }

    }

    private CreateCheckoutRequest buildCheckoutRequest(InitiatePaymentRequest request) {
        // Convert amount to cents (YOCO expects amount in cents)
        long amountInCents = request.getAmount().multiply(new BigDecimal("100")).longValue();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("externalReference", request.getExternalReference());
        metadata.put("customerId", request.getCustomerId());
        metadata.put("timestamp", LocalDateTime.now().toString());
        metadata.put("test_refundable", request.getMetaData().getTestRefundable()== null ? true : request.getMetaData().getTestRefundable() );

        return CreateCheckoutRequest.builder()
                .amount(amountInCents)
                .currency(request.getCurrency() != null ? request.getCurrency() : "ZAR")
                .cancelUrl(cancelUrl)
                .successUrl(successUrl)
                .failureUrl(failureUrl)
                .metadata(metadata)
                .build();
    }

    private PaymentTransaction saveTransaction(
            InitiatePaymentRequest request,
            CheckoutResponse response,
            String idempotencyKey) {

        try {
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .checkoutId(response.getId())
                    .idempotencyKey(idempotencyKey)
                    .externalReference(request.getExternalReference())
                    .customerId(request.getCustomerId())
                    .amount(request.getAmount())
                    .currency(response.getCurrency())
                    .status(PaymentTransaction.PaymentStatus.CREATED)
                    .processingMode(response.getProcessingMode())
                    .redirectUrl(response.getRedirectUrl())
                    .description(request.getDescription())
                    .recipientId(request.getRecipientId())
                    .bookingId(request.getBookingId())
                    .metadata(objectMapper.writeValueAsString(response.getMetadata()))
                    .build();

            return transactionRepository.save(transaction);

        } catch (JsonProcessingException e) {
            log.error("Error serializing metadata", e);
            throw new PaymentException("Failed to save transaction", e);
        }
    }

    private String generateIdempotencyKey(String externalReference) {
    // Round current time down to nearest 2 minutes for idempotency
        long twoMinuteWindow = Instant.now().truncatedTo(ChronoUnit.MINUTES).getEpochSecond() / 120;
        return externalReference + "-" + twoMinuteWindow;
    }


    private PaymentResponse mapToPaymentResponse(PaymentTransaction transaction) {
        return PaymentResponse.builder()
                .checkoutId(transaction.getCheckoutId())
                .redirectUrl(transaction.getRedirectUrl())
                .status(transaction.getStatus().name())
                .externalReference(transaction.getExternalReference())
                .message("Payment initiated successfully")
                .build();
    }
}