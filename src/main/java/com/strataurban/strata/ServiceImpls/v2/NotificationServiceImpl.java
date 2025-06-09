package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.DTOs.v2.NotificationRequest;
import com.strataurban.strata.Entities.Generics.Notification;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.NotificationType;
import com.strataurban.strata.Enums.RecipientType;
import com.strataurban.strata.Enums.ReferenceType;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.NotificationRepository;
import com.strataurban.strata.Services.v2.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, BookingRepository bookingRepository) {
        this.notificationRepository = notificationRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void sendNotification(NotificationRequest notificationRequest) {
        Notification notification = new Notification();
        notification.setRecipientId(notificationRequest.getRecipientId());
        notification.setRecipientType(notificationRequest.getRecipientType());
        notification.setType(notificationRequest.getType());
        notification.setMessage(notificationRequest.getMessage());
        notification.setReferenceId(notificationRequest.getReferenceId());
        notification.setReferenceType(notificationRequest.getReferenceType());
        notification.setMetadata(notificationRequest.getMetadata());
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    @Override
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientId(userId, pageable);
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        notificationRepository.delete(notification);
    }

    @Override
    public void sendBookingRequestNotification(Long bookingId) {
        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        Notification notification = new Notification();
        notification.setRecipientId(booking.getProviderId());
        notification.setRecipientType(RecipientType.PROVIDER);
        notification.setType(NotificationType.BOOKING_REQUEST);
        notification.setMessage("A new booking request has been received: " + booking.getPickUpLocation() + " to " + booking.getDestination());
        notification.setReferenceId(bookingId);
        notification.setReferenceType(ReferenceType.BOOKING);
        notification.setMetadata("{\"url\": \"/bookings/" + bookingId + "\"}");
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    @Override
    public void sendBookingConfirmationNotification(Long bookingId) {
        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        // Assuming the BookingRequest has a clientId field (as noted in the previous step)
        if (booking.getClientId() == null) {
            throw new RuntimeException("Client ID not found for booking ID: " + bookingId);
        }

        Notification notification = new Notification();
        notification.setRecipientId(booking.getClientId());
        notification.setRecipientType(RecipientType.CLIENT);
        notification.setType(NotificationType.BOOKING_CONFIRMED);
        notification.setMessage("Your booking has been confirmed: " + booking.getPickUpLocation() + " to " + booking.getDestination());
        notification.setReferenceId(bookingId);
        notification.setReferenceType(ReferenceType.BOOKING);
        notification.setMetadata("{\"url\": \"/bookings/" + bookingId + "\"}");
        notification.setRead(false);

        notificationRepository.save(notification);
    }
}