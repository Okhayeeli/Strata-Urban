package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.NotificationDTO;
import com.strataurban.strata.DTOs.v2.NotificationRequest;
import com.strataurban.strata.Entities.Generics.Notification;
import com.strataurban.strata.Services.v2.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/notifications")
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationRestController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Send a notification", description = "Sends a notification to a user (Client, Provider, Driver, or Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid notification request")
    })
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest notificationRequest) {
        notificationService.sendNotification(notificationRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications for a user", description = "Fetches all notifications for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificationDTOs);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read", description = "Marks a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification", description = "Deletes a specific notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/booking-request/{bookingId}")
    @Operation(summary = "Send booking request notification", description = "Sends a notification to a provider about a new booking request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Void> sendBookingRequestNotification(@PathVariable Long bookingId) {
        notificationService.sendBookingRequestNotification(bookingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/booking-confirmation/{bookingId}")
    @Operation(summary = "Send booking confirmation notification", description = "Sends a notification to a client about a confirmed booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Void> sendBookingConfirmationNotification(@PathVariable Long bookingId) {
        notificationService.sendBookingConfirmationNotification(bookingId);
        return ResponseEntity.ok().build();
    }

    // Helper method to map Notification entity to NotificationDTO
    private NotificationDTO mapToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getRecipientId(),
                notification.getRecipientType(),
                notification.getType(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getMetadata()
        );
    }
}