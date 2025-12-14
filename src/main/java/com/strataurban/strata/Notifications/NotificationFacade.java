package com.strataurban.strata.Notifications;

import com.strataurban.strata.Entities.Generics.Notification;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.NotificationType;
import com.strataurban.strata.Enums.RecipientType;
import com.strataurban.strata.Enums.ReferenceType;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.NotificationRepository;
import com.strataurban.strata.Repositories.v2.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationFacade {

    private final NotificationDispatcher notificationDispatcher;
    private final NotificationRepository notificationRepository;
    private final RouteRepository routeRepository;
    private final BookingRepository bookingRepository;
    private final NotificationMessageBuilder messageBuilder;

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendNotification(Long recipientId, RecipientType recipientType,
                                                    NotificationType notificationType, String message) {
        try {
            log.info("Sending {} notification to {} {}", notificationType, recipientType, recipientId);
            notificationDispatcher.sendNotification(recipientId, recipientType, notificationType,
                    message, null, null, null);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in sendNotification: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendNotificationWithReference(Long recipientId, RecipientType recipientType,
                                                                 NotificationType notificationType, String message, Long referenceId, ReferenceType referenceType) {
        try {
            log.info("Sending {} notification to {} {} with reference {} {}",
                    notificationType, recipientType, recipientId, referenceType, referenceId);
            notificationDispatcher.sendNotification(recipientId, recipientType, notificationType,
                    message, referenceId, referenceType, null);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in sendNotificationWithReference: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendNotificationFull(Long recipientId, RecipientType recipientType,
                                                        NotificationType notificationType, String message, Long referenceId,
                                                        ReferenceType referenceType, String metadata) {
        try {
            log.info("Sending full {} notification to {} {}", notificationType, recipientType, recipientId);
            notificationDispatcher.sendNotification(recipientId, recipientType, notificationType,
                    message, referenceId, referenceType, metadata);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in sendNotificationFull: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public void notifyBookingSuccessful(Long clientId, Long bookingId, String pickupLocation, String destination) {
        try {
            sendNotificationWithReference(clientId, RecipientType.CLIENT, NotificationType.BOOKING_REQUEST,
                    messageBuilder.bookingSuccessful(pickupLocation, destination), bookingId, ReferenceType.BOOKING);
        } catch (Exception e) {
            log.error("Error in notifyBookingSuccessful: {}", e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    public void notifyOfferReceived(Long providerId, Long bookingId, String offerId) {
        try {
            BookingRequest booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

            sendNotificationWithReference(booking.getClientId(), RecipientType.CLIENT,
                    NotificationType.OFFER_RECEIVED,
                    messageBuilder.offerReceived(booking, providerId, Long.valueOf(offerId)),
                    bookingId, ReferenceType.BOOKING);
        } catch (Exception e) {
            log.error("Error in notifyOfferReceived for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    public void notifyOfferRejection(Long providerId, Long bookingId, String offerId, String reason) {
        try {
            BookingRequest booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

            sendNotification(providerId, RecipientType.PROVIDER, NotificationType.OFFER_REJECTED,
                    messageBuilder.offerRejected(booking, providerId, reason));
        } catch (Exception e) {
            log.error("Error in notifyOfferRejection for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    public void notifyOfferAcceptance(Long providerId, Long bookingId, String offerId, String note) {
        try {
            BookingRequest booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

            sendNotification(providerId, RecipientType.PROVIDER, NotificationType.OFFER_ACCEPTED,
                    messageBuilder.offerAccepted(booking, providerId, Long.valueOf(offerId), note));
        } catch (Exception e) {
            log.error("Error in notifyOfferAcceptance for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyBookingConfirmed(Long clientId, Long bookingId,
                                                          String pickupLocation, String destination) {
        try {
            sendNotificationWithReference(clientId, RecipientType.CLIENT, NotificationType.BOOKING_CONFIRMED,
                    messageBuilder.bookingConfirmed(pickupLocation, destination), bookingId, ReferenceType.BOOKING);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in notifyBookingConfirmed: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyBookingCancelled(Long userId, RecipientType recipientType,
                                                          Long bookingId, String reason) {
        try {
            sendNotificationWithReference(userId, recipientType, NotificationType.BOOKING_CANCELLED,
                    messageBuilder.bookingCancelled(reason), bookingId, ReferenceType.BOOKING);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in notifyBookingCancelled: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyTripStarted(Long clientId, Long tripId, String driverName) {
        try {
            sendNotificationWithReference(clientId, RecipientType.CLIENT, NotificationType.TRIP_STARTED,
                    messageBuilder.tripStarted(driverName), tripId, ReferenceType.TRIP);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in notifyTripStarted: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyTripCompleted(Long clientId, Long tripId, String duration, String fare) {
        try {
            sendNotificationWithReference(clientId, RecipientType.CLIENT, NotificationType.TRIP_COMPLETED,
                    messageBuilder.tripCompleted(duration, fare), tripId, ReferenceType.TRIP);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in notifyTripCompleted: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyPaymentReceived(Long providerId, Long paymentId, String amount) {
        try {
            sendNotificationWithReference(providerId, RecipientType.PROVIDER, NotificationType.PAYMENT_RECEIVED,
                    messageBuilder.paymentReceived(amount), paymentId, ReferenceType.PAYMENT);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in notifyPaymentReceived: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyDriverAssigned(Long clientId, Long bookingId,
                                                        String driverName, String vehicleInfo) {
        try {
            sendNotificationWithReference(clientId, RecipientType.CLIENT, NotificationType.DRIVER_ASSIGNED,
                    messageBuilder.driverAssigned(driverName, vehicleInfo), bookingId, ReferenceType.BOOKING);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in notifyDriverAssigned: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public void notifyPaymentSuccessful(Long clientId, Long providerId, Long bookingId,
                                        String amount, String bookingReference) {
        try {
            log.info("Notifying payment success for booking {} - Client: {}, Provider: {}",
                    bookingId, clientId, providerId);

            sendNotificationWithReference(clientId, RecipientType.CLIENT,
                    NotificationType.PAYMENT_SUCCESSFUL,
                    messageBuilder.paymentSuccessfulClient(amount, bookingReference),
                    bookingId, ReferenceType.BOOKING);

            sendNotificationWithReference(providerId, RecipientType.PROVIDER,
                    NotificationType.PAYMENT_SUCCESSFUL,
                    messageBuilder.paymentSuccessfulProvider(amount, bookingReference),
                    bookingId, ReferenceType.BOOKING);

            log.info("Payment success notifications sent for booking {}", bookingId);
        } catch (Exception e) {
            log.error("Error in notifyPaymentSuccessful for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyProvidersOnRoute(Long routeId, Long bookingId,
                                                          String pickupLocation, String destination) {
        if (routeId == null) {
            log.warn("Cannot notify providers - routeId is null for booking {}", bookingId);
            return CompletableFuture.completedFuture(null);
        }

        log.info("Notifying providers on route {} about booking {}", routeId, bookingId);

        try {
            Routes route = routeRepository.findById(routeId).orElse(null);
            if (route == null) {
                log.warn("Route {} not found for booking {}", routeId, bookingId);
                return CompletableFuture.completedFuture(null);
            }

            List<String> providerIds = route.getProviderIdList();
            if (providerIds.isEmpty()) {
                log.warn("No providers on route {} for booking {}", routeId, bookingId);
                return CompletableFuture.completedFuture(null);
            }

            log.info("Found {} provider(s) on route {}", providerIds.size(), routeId);

            for (String providerId : providerIds) {
                try {
                    Long providerIdLong = Long.parseLong(providerId);
                    sendNotificationWithReference(providerIdLong, RecipientType.PROVIDER,
                            NotificationType.BOOKING_REQUEST,
                            messageBuilder.providerBookingRequest(providerIdLong, pickupLocation, destination),
                            bookingId, ReferenceType.BOOKING);
                    log.debug("Notified provider {} about booking {}", providerIdLong, bookingId);
                } catch (NumberFormatException e) {
                    log.error("Invalid provider ID '{}' in route {}", providerId, routeId);
                } catch (Exception e) {
                    log.error("Error notifying provider {} on route {}: {}", providerId, routeId, e.getMessage());
                }
            }

            log.info("Notified {} provider(s) on route {}", providerIds.size(), routeId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error notifying providers on route {}: {}", routeId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user {}", userId);
        return notificationRepository.findByRecipientId(userId, pageable);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        log.info("Marking notification {} as read", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);
        Page<Notification> notifications = notificationRepository.findByRecipientId(userId, Pageable.unpaged());
        notifications.forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(LocalDateTime.now());
            }
        });
        notificationRepository.saveAll(notifications);
        log.info("Marked {} notifications as read for user {}", notifications.getTotalElements(), userId);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notificationRepository.delete(notification);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        log.debug("Getting unread count for user {}", userId);
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }
}