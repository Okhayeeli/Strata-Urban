package com.strataurban.strata.Services.v2;

import com.strataurban.strata.DTOs.v2.NotificationRequest;
import com.strataurban.strata.Entities.Generics.Notification;

import java.util.List;

public interface NotificationService {

    // Send a notification to a user
    void sendNotification(NotificationRequest notificationRequest);

    // Get all notifications for a user
    List<Notification> getUserNotifications(Long userId);

    // Mark a notification as read
    void markAsRead(Long id);

    // Delete a notification
    void deleteNotification(Long id);

    // Send a booking request notification to a provider
    void sendBookingRequestNotification(Long bookingId);

    // Send a booking confirmation notification to a client
    void sendBookingConfirmationNotification(Long bookingId);
}