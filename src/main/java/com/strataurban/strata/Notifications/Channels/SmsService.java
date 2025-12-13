package com.strataurban.strata.Notifications.Channels;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * SMS Service for sending text messages
 * TODO: Integrate with actual SMS provider (Twilio, AWS SNS, Africa's Talking, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${sms.provider:mock}")
    private String smsProvider;

    /**
     * Send SMS asynchronously
     */
    @Async("notificationExecutor")
    public CompletableFuture<Boolean> sendSms(String phoneNumber, String message) {
        try {
            if (!smsEnabled) {
                log.warn("SMS is disabled. Would have sent to {}: {}", phoneNumber, message);
                return CompletableFuture.completedFuture(false);
            }

            log.info("Sending SMS to {} via provider {}", phoneNumber, smsProvider);

            // TODO: Replace with actual SMS provider integration
            // Example integrations:
            // - Twilio: twilioClient.messages().create(...)
            // - AWS SNS: snsClient.publish(...)
            // - Africa's Talking: africastalking.sms().send(...)

            // Simulate SMS sending
            Thread.sleep(500); // Simulate network delay

            log.info("SMS sent successfully to: {}", phoneNumber);
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Validate phone number format
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Basic validation - adjust regex based on your region
        // This accepts formats like: +234XXXXXXXXXX, 234XXXXXXXXXX, 0XXXXXXXXXX
        String cleaned = phoneNumber.replaceAll("[\\s-()]", "");
        return cleaned.matches("^(\\+?234|0)?[7-9][0-1]\\d{8}$");
    }

    /**
     * Format phone number to E.164 format
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        String cleaned = phoneNumber.replaceAll("[\\s-()]", "");

        // Convert to international format if needed
        if (cleaned.startsWith("0")) {
            cleaned = "+234" + cleaned.substring(1);
        } else if (cleaned.startsWith("234") && !cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        } else if (!cleaned.startsWith("+")) {
            cleaned = "+234" + cleaned;
        }

        return cleaned;
    }
}