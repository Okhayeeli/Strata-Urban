package com.strataurban.strata.yoco_integration.services;

import com.strataurban.strata.yoco_integration.config.YocoProperties;
import com.strataurban.strata.yoco_integration.dtos.RefundResponse;
import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import com.strataurban.strata.yoco_integration.entities.RefundTransaction;
import com.strataurban.strata.yoco_integration.exceptions.PaymentException;
import com.strataurban.strata.yoco_integration.repositories.PaymentTransactionRepository;
import com.strataurban.strata.yoco_integration.repositories.RefundTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RestTemplate yocoRestTemplate;
    private final RefundTransactionRepository refundRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final YocoProperties yocoProperties;

    /**
     * Processes a refund for a payment
     */
    @Transactional
    public RefundResponse processRefund(String checkoutId, BigDecimal refundAmount,
                                        String reason, String initiatedBy) {

        // Get original payment transaction
        PaymentTransaction payment = transactionRepository.findByCheckoutId(checkoutId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + checkoutId));

        // Validate refund
        validateRefund(payment, refundAmount);

        // Call YOCO API to process refund
        RefundResponse apiResponse = initiateRefundWithYoco(checkoutId);

        // Store refund transaction
        RefundTransaction refund = RefundTransaction.builder()
                .refundId(apiResponse.getRefundId())
                .paymentTransactionId(payment.getId())
                .checkoutId(checkoutId)
                .paymentId(payment.getPaymentId())
                .refundAmount(refundAmount)
                .currency(payment.getCurrency())
                .status(RefundTransaction.RefundStatus.PENDING)
                .reason(reason)
                .initiatedBy(initiatedBy)
                .build();

        refundRepository.save(refund);

        log.info("Refund initiated: CheckoutId={}, RefundId={}, Amount={}",
                checkoutId, apiResponse.getRefundId(), refundAmount);

        return apiResponse;
    }

    /**
     * Updates refund status (called by webhook handler)
     */
    @Transactional
    public void updateRefundStatus(String refundId, boolean succeeded, String errorMessage) {

        RefundTransaction refund = refundRepository.findByRefundId(refundId)
                .orElseThrow(() -> new PaymentException("Refund not found: " + refundId));

        refund.setStatus(succeeded ?
                RefundTransaction.RefundStatus.SUCCEEDED :
                RefundTransaction.RefundStatus.FAILED);

        if (errorMessage != null) {
            refund.setErrorMessage(errorMessage);
        }

        refund.setCompletedAt(LocalDateTime.now());
        refundRepository.save(refund);

        // Update original payment status if full refund
        if (succeeded) {
            updatePaymentRefundStatus(refund);
        }

        log.info("Refund status updated: RefundId={}, Status={}",
                refundId, refund.getStatus());
    }

    private RefundResponse initiateRefundWithYoco(String checkoutId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(yocoProperties.getApi().getSecretKey());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<RefundResponse> response = yocoRestTemplate.exchange(
                    "/checkouts/" + checkoutId + "/refund",
                    HttpMethod.POST,
                    entity,
                    RefundResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }

            throw new PaymentException("Failed to initiate refund with YOCO");

        } catch (Exception e) {
            log.error("Error initiating refund", e);
            throw new PaymentException("Failed to process refund", e);
        }
    }

    private void validateRefund(PaymentTransaction payment, BigDecimal refundAmount) {
        if (payment.getStatus() != PaymentTransaction.PaymentStatus.SUCCEEDED) {
            throw new PaymentException("Cannot refund payment with status: " + payment.getStatus());
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Refund amount exceeds payment amount");
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Invalid refund amount");
        }
    }

    private void updatePaymentRefundStatus(RefundTransaction refund) {
        PaymentTransaction payment = transactionRepository.findById(refund.getPaymentTransactionId())
                .orElseThrow(() -> new PaymentException("Payment not found"));

        // Check if this is a full or partial refund
        if (refund.getRefundAmount().compareTo(payment.getAmount()) == 0) {
            payment.setStatus(PaymentTransaction.PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(PaymentTransaction.PaymentStatus.PARTIALLY_REFUNDED);
        }

        transactionRepository.save(payment);
    }
}
