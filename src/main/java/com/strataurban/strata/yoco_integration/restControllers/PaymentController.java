package com.strataurban.strata.yoco_integration.restControllers;

import com.strataurban.strata.Security.LoggedUser;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.yoco_integration.dtos.InitiatePaymentRequest;
import com.strataurban.strata.yoco_integration.dtos.PaymentResponse;
import com.strataurban.strata.yoco_integration.dtos.RefundRequest;
import com.strataurban.strata.yoco_integration.dtos.RefundResponse;
import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import com.strataurban.strata.yoco_integration.services.PaymentService;
import com.strataurban.strata.yoco_integration.services.RefundService;
import com.strataurban.strata.yoco_integration.services.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final WebhookService webhookService;
    private final RefundService refundService;

    /**
     * Initiates a new payment with YOCO
     *
     * @param request Payment initiation request
     * @return Payment response with redirect URL
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody InitiatePaymentRequest request, @LoggedUser SecurityUserDetails userDetails) {
        PaymentResponse response = paymentService.initiatePayment(request, userDetails);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/status/{checkoutId}")
    public ResponseEntity<PaymentTransaction> getPaymentStatus(@PathVariable String checkoutId) {

        log.debug("Retrieving payment status for checkoutId: {}", checkoutId);

        PaymentTransaction transaction = paymentService.getPaymentByCheckoutId(checkoutId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/reference/{externalReference}")
    public ResponseEntity<PaymentTransaction> getPaymentByReference(@PathVariable String externalReference) {

        log.debug("Retrieving payment for externalReference: {}", externalReference);
        PaymentTransaction transaction = paymentService.getPaymentByExternalReference(externalReference);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(HttpServletRequest request) {
        return buildWebhookResponse(request);
    }

    private ResponseEntity<Void> buildWebhookResponse(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // Extract raw body (needed for signature verification)
            String rawPayload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            // Extract webhook headers
            Map<String, String> headers = extractWebhookHeaders(request);

            log.info("Received webhook: EventId={}, Type={}", headers.get("webhook-id"), headers.get("webhook-type"));

            // Process webhook asynchronously to respond quickly
            // IMPORTANT: In production, use async processing or queue
            webhookService.processWebhook(rawPayload, headers);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Webhook handled in {}ms", duration);
            return ResponseEntity.ok().build();

        } catch (IOException e) {
            log.error("Error reading webhook payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            // Still return 200 to prevent YOCO from retrying immediately
            // Failed webhooks are stored for manual review
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> processRefund(@Valid @RequestBody RefundRequest request, @LoggedUser SecurityUserDetails userDetails) {

        log.info("Processing refund for checkoutId: {}, amount: {}, reason: {}", request.getCheckoutId(), request.getAmount(), request.getReason());
        RefundResponse response = refundService.processRefund(request.getCheckoutId(), request.getAmount(), request.getReason(), userDetails.getFullName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-service");
        return ResponseEntity.ok(response);
    }

    private Map<String, String> extractWebhookHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.startsWith("webhook-")) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }

        return headers;
    }
}
