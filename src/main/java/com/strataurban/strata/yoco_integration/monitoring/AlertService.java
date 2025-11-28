package com.strataurban.strata.yoco_integration.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Sends alerts for critical payment issues
 * Integrate with your alerting system (PagerDuty, Slack, Email, etc.)
 */
@Service
@Slf4j
public class AlertService {

    public void sendAlert(String alertType, String message) {
        log.error("ALERT [{}]: {}", alertType, message);

        // TODO: Integrate with your alerting system
        // Examples:
        // - Send to Slack
        // - Send to PagerDuty
        // - Send email
        // - Trigger SMS

        switch (alertType) {
            case "HIGH_FAILURE_RATE":
                sendHighPriorityAlert(message);
                break;
            case "STALE_TRANSACTIONS":
                sendMediumPriorityAlert(message);
                break;
            case "WEBHOOK_VALIDATION_FAILURE":
                sendHighPriorityAlert(message);
                break;
            default:
                sendLowPriorityAlert(message);
        }
    }

    private void sendHighPriorityAlert(String message) {
        // Send to on-call team immediately
        log.error("HIGH PRIORITY ALERT: {}", message);
    }

    private void sendMediumPriorityAlert(String message) {
        // Send to monitoring channel
        log.warn("MEDIUM PRIORITY ALERT: {}", message);
    }

    private void sendLowPriorityAlert(String message) {
        // Log for review
        log.info("LOW PRIORITY ALERT: {}", message);
    }
}
