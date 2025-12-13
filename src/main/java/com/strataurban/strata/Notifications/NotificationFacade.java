package com.strataurban.strata.Notifications;

import com.strataurban.strata.Entities.Generics.Notification;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Enums.NotificationType;
import com.strataurban.strata.Enums.RecipientType;
import com.strataurban.strata.Enums.ReferenceType;
import com.strataurban.strata.Repositories.v2.NotificationRepository;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Repositories.v2.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main notification facade service that other services should use
 * This provides simple, clean methods to send notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationFacade {

    private final NotificationDispatcher notificationDispatcher;
    private final NotificationRepository notificationRepository;
    private final RouteRepository routeRepository;
    private final ProviderRepository providerRepository;

    /**
     * Simple method to send notification - just provide message and basic info
     * The dispatcher handles everything else (checking preferences, sending to channels, etc.)
     */
    public void sendNotification(
            Long recipientId,
            RecipientType recipientType,
            NotificationType notificationType,
            String message) {

        log.info("Sending {} notification to {} {}", notificationType, recipientType, recipientId);

        notificationDispatcher.sendNotification(
                recipientId,
                recipientType,
                notificationType,
                message,
                null,
                null,
                null
        );
    }

    /**
     * Send notification with reference to related entity
     */
    public void sendNotificationWithReference(
            Long recipientId,
            RecipientType recipientType,
            NotificationType notificationType,
            String message,
            Long referenceId,
            ReferenceType referenceType) {

        log.info("Sending {} notification to {} {} with reference {} {}",
                notificationType, recipientType, recipientId, referenceType, referenceId);

        notificationDispatcher.sendNotification(
                recipientId,
                recipientType,
                notificationType,
                message,
                referenceId,
                referenceType,
                null
        );
    }

    /**
     * Send notification with all optional parameters
     */
    public void sendNotificationFull(
            Long recipientId,
            RecipientType recipientType,
            NotificationType notificationType,
            String message,
            Long referenceId,
            ReferenceType referenceType,
            String metadata) {

        log.info("Sending full {} notification to {} {}", notificationType, recipientType, recipientId);

        notificationDispatcher.sendNotification(
                recipientId,
                recipientType,
                notificationType,
                message,
                referenceId,
                referenceType,
                metadata
        );
    }

    // ===== CONVENIENCE METHODS FOR COMMON SCENARIOS =====

    /**
     * Send booking request notification to provider
     */
    public void notifyBookingRequest(Long providerId, Long bookingId, String pickupLocation, String destination) {
        String message = String.format("New booking request from %s to %s", pickupLocation, destination);
        sendNotificationWithReference(
                providerId,
                RecipientType.PROVIDER,
                NotificationType.BOOKING_REQUEST,
                message,
                bookingId,
                ReferenceType.BOOKING
        );
    }


    public void notifyBookingSuccessful(Long clientId, Long bookingId, String pickupLocation, String destination) {
        String message = String.format("Your booking from %s to %s has been successfully initiated", pickupLocation, destination);
        sendNotificationWithReference(
                clientId,
                RecipientType.CLIENT,
                NotificationType.BOOKING_REQUEST,
                message,
                bookingId,
                ReferenceType.BOOKING
        );
    }

    /**
     * Send booking confirmation to client
     */
    public void notifyBookingConfirmed(Long clientId, Long bookingId, String pickupLocation, String destination) {
        String message = String.format("Your booking from %s to %s has been confirmed", pickupLocation, destination);
        sendNotificationWithReference(
                clientId,
                RecipientType.CLIENT,
                NotificationType.BOOKING_CONFIRMED,
                message,
                bookingId,
                ReferenceType.BOOKING
        );
    }

    /**
     * Send booking cancellation notification
     */
    public void notifyBookingCancelled(Long userId, RecipientType recipientType, Long bookingId, String reason) {
        String message = String.format("Booking has been cancelled. Reason: %s", reason);
        sendNotificationWithReference(
                userId,
                recipientType,
                NotificationType.BOOKING_CANCELLED,
                message,
                bookingId,
                ReferenceType.BOOKING
        );
    }

    /**
     * Send trip started notification
     */
    public void notifyTripStarted(Long clientId, Long tripId, String driverName) {
        String message = String.format("Your trip with driver %s has started", driverName);
        sendNotificationWithReference(
                clientId,
                RecipientType.CLIENT,
                NotificationType.TRIP_STARTED,
                message,
                tripId,
                ReferenceType.TRIP
        );
    }

    /**
     * Send trip completed notification
     */
    public void notifyTripCompleted(Long clientId, Long tripId, String duration, String fare) {
        String message = String.format("Your trip has been completed. Duration: %s, Fare: %s", duration, fare);
        sendNotificationWithReference(
                clientId,
                RecipientType.CLIENT,
                NotificationType.TRIP_COMPLETED,
                message,
                tripId,
                ReferenceType.TRIP
        );
    }

    /**
     * Send payment received notification
     */
    public void notifyPaymentReceived(Long providerId, Long paymentId, String amount) {
        String message = String.format("Payment of %s has been received", amount);
        sendNotificationWithReference(
                providerId,
                RecipientType.PROVIDER,
                NotificationType.PAYMENT_RECEIVED,
                message,
                paymentId,
                ReferenceType.PAYMENT
        );
    }

    /**
     * Send driver assigned notification
     */
    public void notifyDriverAssigned(Long clientId, Long bookingId, String driverName, String vehicleInfo) {
        String message = String.format("Driver %s has been assigned to your booking. Vehicle: %s",
                driverName, vehicleInfo);
        sendNotificationWithReference(
                clientId,
                RecipientType.CLIENT,
                NotificationType.DRIVER_ASSIGNED,
                message,
                bookingId,
                ReferenceType.BOOKING
        );
    }

    // ===== QUERY METHODS FOR IN-APP NOTIFICATIONS =====

    /**
     * Get user's in-app notifications (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user {}", userId);
        return notificationRepository.findByRecipientId(userId, pageable);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        log.info("Marking notification {} as read", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("Notification {} marked as read", notificationId);
    }

    /**
     * Mark all user notifications as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);

        Page<Notification> notifications = notificationRepository.findByRecipientId(
                userId, Pageable.unpaged());

        notifications.forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(LocalDateTime.now());
            }
        });

        notificationRepository.saveAll(notifications);
        log.info("Marked {} notifications as read for user {}", notifications.getTotalElements(), userId);
    }

    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        notificationRepository.delete(notification);
        log.info("Notification {} deleted", notificationId);
    }

    /**
     * Get unread notification count for user
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        // You'll need to add this query to NotificationRepository
        log.debug("Getting unread count for user {}", userId);
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    /**
     * Notify ALL providers assigned to a route about a new booking
     * This is called when a client creates a booking
     *
     * @param routeId The route ID of the booking
     * @param bookingId The booking ID
     * @param pickupLocation Pickup location
     * @param destination Destination location
     */
    public void notifyProvidersOnRoute(Long routeId, Long bookingId, String pickupLocation, String destination) {
        if (routeId == null) {
            log.warn("Cannot notify providers - routeId is null for booking {}", bookingId);
            return;
        }

        log.info("Notifying all providers on route {} about new booking {}", routeId, bookingId);

        try {
            // Get the route
            Routes route = routeRepository.findById(routeId).orElse(null);

            if (route == null) {
                log.warn("Route {} not found for booking {}", routeId, bookingId);
                return;
            }

            // Get all provider IDs assigned to this route
            List<String> providerIds = route.getProviderIdList();

            if (providerIds.isEmpty()) {
                log.warn("No providers assigned to route {} for booking {}", routeId, bookingId);
                return;
            }

            log.info("Found {} provider(s) on route {} - sending notifications", providerIds.size(), routeId);


            for (String providerId : providerIds) {
                try {
                    Long providerIdLong = Long.parseLong(providerId);

                    sendNotificationWithReference(
                            providerIdLong,
                            RecipientType.PROVIDER,
                            NotificationType.BOOKING_REQUEST,
                            message(providerIdLong, pickupLocation, destination),
                            bookingId,
                            ReferenceType.BOOKING
                    );

                    log.debug("Notified provider {} about booking {}", providerIdLong, bookingId);

                } catch (NumberFormatException e) {
                    log.error("Invalid provider ID '{}' in route {} - skipping", providerId, routeId, e);
                }
            }

            log.info("Successfully notified {} provider(s) on route {} about booking {}",
                    providerIds.size(), routeId, bookingId);

        } catch (Exception e) {
            log.error("Error notifying providers on route {} about booking {}: {}",
                    routeId, bookingId, e.getMessage(), e);
        }
    }

    private String message(Long providerId, String pickupLocation, String destination) {
        Provider provider = providerRepository.findById(providerId).orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));
        return String.format("Dear %s New booking request from %s to %s", provider.getCompanyName(),  pickupLocation, destination);
    }

}