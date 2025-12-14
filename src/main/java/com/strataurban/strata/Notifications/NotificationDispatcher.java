package com.strataurban.strata.Notifications;

import com.strataurban.strata.Entities.Generics.Notification;
import com.strataurban.strata.Enums.NotificationType;
import com.strataurban.strata.Enums.RecipientType;
import com.strataurban.strata.Enums.ReferenceType;
import com.strataurban.strata.Notifications.Channels.EmailService2;
import com.strataurban.strata.Notifications.Channels.PushNotificationService;
import com.strataurban.strata.Notifications.Channels.SmsService;
import com.strataurban.strata.Notifications.Repository.NotificationPreferenceRepository;
import com.strataurban.strata.Repositories.v2.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Core notification dispatcher that handles routing notifications
 * to appropriate channels based on user preferences
 *
 * Simple model: User enables channels (EMAIL, SMS, PUSH, IN_APP)
 * and receives ALL notification types via those channels
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService2 emailService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;
    private final UserContactService userContactService;

    /**
     * Main method to send notifications - checks user preferences and dispatches accordingly
     *
     * @param recipientId The user ID receiving the notification
     * @param recipientType The type of recipient (CLIENT, PROVIDER, DRIVER)
     * @param notificationType The type of notification (for display/categorization only)
     * @param message The notification message
     * @param referenceId Optional reference to related entity
     * @param referenceType Optional type of reference
     * @param metadata Optional additional data
     */
    @Async("notificationExecutor")
    @Transactional
    public CompletableFuture<Void> sendNotification(
            Long recipientId,
            RecipientType recipientType,
            NotificationType notificationType,
            String message,
            Long referenceId,
            ReferenceType referenceType,
            String metadata) {

        try {
            log.info("Processing {} notification for user {}", notificationType, recipientId);

            // Get enabled channels for this user (applies to ALL notification types)
            List<NotificationChannel> enabledChannels = preferenceRepository.findEnabledChannels(recipientId);

            // If no preferences set, default to IN_APP only
            if (enabledChannels == null || enabledChannels.isEmpty()) {
                log.info("No preferences found for user {}. Defaulting to IN_APP notification", recipientId);
                enabledChannels = List.of(NotificationChannel.IN_APP);
            }

            log.info("User {} has {} enabled channel(s): {}",
                    recipientId, enabledChannels.size(), enabledChannels);

            // Get user contact information once
            var userContact = userContactService.getUserContact(recipientId);

            // Dispatch to each enabled channel
            for (NotificationChannel channel : enabledChannels) {
                dispatchToChannel(
                        channel,
                        recipientId,
                        recipientType,
                        notificationType,
                        message,
                        referenceId,
                        referenceType,
                        metadata,
                        userContact);
            }

            log.info("Notification processing completed for user {}", recipientId);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Error processing notification for user {}: {}", recipientId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Dispatch notification to a specific channel
     */
    private void dispatchToChannel(
            NotificationChannel channel,
            Long recipientId,
            RecipientType recipientType,
            NotificationType notificationType,
            String message,
            Long referenceId,
            ReferenceType referenceType,
            String metadata,
            UserContactService.UserContact userContact) {

        try {
            log.info("Dispatching {} notification to channel {} for user {}",
                    notificationType, channel, recipientId);

            CompletableFuture<Boolean> sendResult = switch (channel) {
                case EMAIL -> {
                    if (userContact.email() == null || userContact.email().isEmpty()) {
                        log.warn("No email found for user {} - skipping EMAIL channel", recipientId);
                        yield CompletableFuture.completedFuture(false);
                    }
                    String subject = getEmailSubject(notificationType);
                    yield emailService.sendEmailAsync(userContact.email(), subject, message);
                }
                case SMS -> {
                    if (userContact.phoneNumber() == null || userContact.phoneNumber().isEmpty()) {
                        log.warn("No phone number found for user {} - skipping SMS channel", recipientId);
                        yield CompletableFuture.completedFuture(false);
                    }
                    yield smsService.sendSms(userContact.phoneNumber(), message);
                }
                case PUSH -> {
                    if (userContact.deviceToken() == null || userContact.deviceToken().isEmpty()) {
                        log.warn("No device token found for user {} - skipping PUSH channel", recipientId);
                        yield CompletableFuture.completedFuture(false);
                    }
                    String title = getPushTitle(notificationType);
                    yield pushNotificationService.sendPushNotification(
                            userContact.deviceToken(), title, message, metadata);
                }
                case IN_APP -> {
                    // For in-app, we just save to database
                    saveInAppNotification(recipientId, recipientType, notificationType,
                            message, referenceId, referenceType, metadata, channel);
                    yield CompletableFuture.completedFuture(true);
                }
            };

            // Handle result
            sendResult.whenComplete((success, error) -> {
                if (error != null) {
                    log.error("Failed to send {} notification via {} to user {}: {}",
                            notificationType, channel, recipientId, error.getMessage());
                    saveFailedNotification(recipientId, recipientType, notificationType,
                            message, referenceId, referenceType, metadata, channel, error.getMessage());
                } else if (success) {
                    log.info("{} notification sent successfully via {} to user {}",
                            notificationType, channel, recipientId);
                    // For non-IN_APP channels, also save to database for record keeping
                    if (channel != NotificationChannel.IN_APP) {
                        saveSuccessfulNotification(recipientId, recipientType, notificationType,
                                message, referenceId, referenceType, metadata, channel);
                    }
                } else {
                    log.warn("{} notification failed to send via {} to user {} (returned false)",
                            notificationType, channel, recipientId);
                    saveFailedNotification(recipientId, recipientType, notificationType,
                            message, referenceId, referenceType, metadata, channel,
                            "Service returned false - likely missing contact info");
                }
            });

        } catch (Exception e) {
            log.error("Error dispatching {} via {} for user {}: {}",
                    notificationType, channel, recipientId, e.getMessage(), e);
        }
    }

    /**
     * Save successful notification to database
     */
    private void saveSuccessfulNotification(
            Long recipientId,
            RecipientType recipientType,
            NotificationType notificationType,
            String message,
            Long referenceId,
            ReferenceType referenceType,
            String metadata,
            NotificationChannel channel) {

        try {
            Notification notification = Notification.builder()
                    .recipientId(recipientId)
                    .recipientType(recipientType)
                    .type(notificationType)
                    .channel(channel)
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .metadata(metadata)
                    .isRead(channel != NotificationChannel.IN_APP) // Non-IN_APP are auto-marked as "read"
                    .deliveryStatus("SENT")
                    .build();

            notificationRepository.save(notification);
            log.debug("Notification saved to database for user {} via {}", recipientId, channel);
        } catch (Exception e) {
            log.error("Failed to save notification to database: {}", e.getMessage(), e);
        }
    }

    /**
     * Save failed notification to database
     */
    private void saveFailedNotification(
            Long recipientId,
            RecipientType recipientType,
            NotificationType notificationType,
            String message,
            Long referenceId,
            ReferenceType referenceType,
            String metadata,
            NotificationChannel channel,
            String errorMessage) {

        try {
            Notification notification = Notification.builder()
                    .recipientId(recipientId)
                    .recipientType(recipientType)
                    .type(notificationType)
                    .channel(channel)
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .metadata(metadata)
                    .isRead(false)
                    .deliveryStatus("FAILED")
                    .errorMessage(errorMessage)
                    .build();

            notificationRepository.save(notification);
            log.debug("Failed notification saved to database for user {} via {}", recipientId, channel);
        } catch (Exception e) {
            log.error("Failed to save failed notification to database: {}", e.getMessage(), e);
        }
    }

    /**
     * Save in-app notification to database
     */
    private void saveInAppNotification(
            Long recipientId,
            RecipientType recipientType,
            NotificationType notificationType,
            String message,
            Long referenceId,
            ReferenceType referenceType,
            String metadata,
            NotificationChannel channel) {

        try {
            Notification notification = Notification.builder()
                    .recipientId(recipientId)
                    .recipientType(recipientType)
                    .type(notificationType)
                    .channel(channel)
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .metadata(metadata)
                    .isRead(false)
                    .deliveryStatus("DELIVERED")
                    .build();

            notificationRepository.save(notification);
            log.info("In-app notification saved for user {}", recipientId);
        } catch (Exception e) {
            log.error("Failed to save in-app notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate email subject based on notification type
     */
    private String getEmailSubject(NotificationType type) {
        return switch (type) {
            case BOOKING_REQUEST -> "New Booking Request";
            case BOOKING_CONFIRMED -> "Booking Confirmed";
            case BOOKING_CANCELLED -> "Booking Cancelled";
            case OFFER_RECEIVED -> "Booking Offer Received";
            case OFFER_REJECTED -> "Offer Rejection";
            case OFFER_ACCEPTED -> "Offer Accepted";
            case TRIP_STARTED -> "Trip Started";
            case TRIP_ENDED, TRIP_COMPLETED -> "Trip Completed";
            case PAYMENT_RECEIVED, PAYMENT_SUCCESSFUL -> "Payment Received";
            case PAYMENT_FAILED -> "Payment Failed";
            case DRIVER_ASSIGNED -> "Driver Assigned";
            case NEW_MESSAGE -> "New Message";
            case ACCOUNT_SUSPENDED -> "Account Status Update";
            case RATING_REQUEST -> "Rate Your Trip";
            default -> "Notification from Strata";
        };
    }

    /**
     * Generate push notification title based on notification type
     */
    private String getPushTitle(NotificationType type) {
        return switch (type) {
            case BOOKING_REQUEST -> "New Booking";
            case BOOKING_CONFIRMED -> "Booking Confirmed";
            case BOOKING_CANCELLED -> "Booking Cancelled";
            case TRIP_STARTED -> "Trip Started";
            case TRIP_ENDED, TRIP_COMPLETED -> "Trip Completed";
            case PAYMENT_RECEIVED, PAYMENT_SUCCESSFUL -> "Payment Received";
            case PAYMENT_FAILED -> "Payment Failed";
            case DRIVER_ASSIGNED -> "Driver Assigned";
            case NEW_MESSAGE -> "New Message";
            case ACCOUNT_SUSPENDED -> "Account Update";
            case RATING_REQUEST -> "Rate Your Trip";
            default -> "Strata Update";
        };
    }
}