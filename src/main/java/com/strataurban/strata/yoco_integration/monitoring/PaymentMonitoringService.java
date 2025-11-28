package com.strataurban.strata.yoco_integration.monitoring;

import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import com.strataurban.strata.yoco_integration.repositories.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Monitors payment health without requiring Micrometer
 * Simple logging-based monitoring for development
 * Can be enhanced with Micrometer/Prometheus later
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMonitoringService {

    private final PaymentTransactionRepository transactionRepository;
    private final AlertService alertService;

    private static final double FAILURE_THRESHOLD = 0.10; // 10% failure rate
    private static final int MONITORING_WINDOW_MINUTES = 15;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void monitorPaymentHealth() {
        log.debug("Running payment health monitoring...");

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(MONITORING_WINDOW_MINUTES);

        // Count successful and failed payments
        long successCount = transactionRepository.countByStatusAndCreatedAfter(
                PaymentTransaction.PaymentStatus.SUCCEEDED, windowStart);

        long failureCount = transactionRepository.countByStatusAndCreatedAfter(
                PaymentTransaction.PaymentStatus.FAILED, windowStart);

        long totalCount = successCount + failureCount;

        if (totalCount > 0) {
            double failureRate = (double) failureCount / totalCount;

            // Log metrics
            log.info("Payment Health Check - Success: {}, Failures: {}, Rate: {:.2f}%",
                    successCount, failureCount, failureRate * 100);

            // Alert if failure rate exceeds threshold
            if (failureRate > FAILURE_THRESHOLD) {
                alertService.sendAlert(
                        "HIGH_FAILURE_RATE",
                        String.format("Payment failure rate: %.2f%% (%d failures in %d transactions)",
                                failureRate * 100, failureCount, totalCount)
                );
            }
        }
    }

    @Scheduled(fixedRate = 600000) // Every 10 minutes
    public void monitorStaleTransactions() {
        log.debug("Checking for stale transactions...");

        // Find transactions stuck in CREATED status for more than 30 minutes
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(30);

        List<PaymentTransaction> staleTransactions = transactionRepository.findStaleTransactions(
                PaymentTransaction.PaymentStatus.CREATED, staleThreshold);

        if (!staleTransactions.isEmpty()) {
            log.warn("Found {} stale transactions", staleTransactions.size());
            alertService.sendAlert(
                    "STALE_TRANSACTIONS",
                    String.format("Found %d stale transactions (created > 30 minutes ago)",
                            staleTransactions.size())
            );
        }
    }

    /**
     * Simple logging-based metrics (can be enhanced with Micrometer later)
     */
    public void recordPaymentInitiated() {
        log.info("Payment initiated");
    }

    public void recordPaymentSuccess() {
        log.info("Payment succeeded");
    }

    public void recordPaymentFailure(String reason) {
        log.warn("Payment failed: {}", reason);
    }

    public void recordWebhookReceived(String eventType) {
        log.info("Webhook received: {}", eventType);
    }

    public void recordWebhookProcessed(String eventType) {
        log.info("Webhook processed: {}", eventType);
    }
}
