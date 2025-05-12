package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.NotificationDTO;
import com.strataurban.strata.DTOs.v2.NotificationRequest;
import com.strataurban.strata.Entities.Generics.Notification;
import com.strataurban.strata.Services.v2.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
            @ApiResponse(responseCode = "400", description = "Invalid notification request"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN can send notifications. CLIENT, PROVIDER, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest notificationRequest) {
        try {
            notificationService.sendNotification(notificationRequest);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN can send notifications. CLIENT, PROVIDER, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications for a user", description = "Fetches all notifications for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only the user (CLIENT, PROVIDER, DRIVER with principal.id == #userId), ADMIN, or DEVELOPER can access this endpoint. Others are restricted.")
    })
    @PreAuthorize("((hasRole('CLIENT') or hasRole('PROVIDER') or hasRole('DRIVER')) and principal.id == #userId) or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            List<NotificationDTO> notificationDTOs = notifications.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(notificationDTOs);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only the user (CLIENT, PROVIDER, DRIVER with principal.id == #userId), ADMIN, or DEVELOPER can access this endpoint. Others are restricted.");
        }
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read", description = "Marks a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only the user (CLIENT, PROVIDER, DRIVER if authorized) or ADMIN can mark a notification as read. DEVELOPER and others are restricted.")
    })
    @PreAuthorize("((hasRole('CLIENT') or hasRole('PROVIDER') or hasRole('DRIVER')) and @notificationService.isAuthorizedUserNotification(#id, principal.id)) or hasRole('ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only the user (CLIENT, PROVIDER, DRIVER if authorized) or ADMIN can mark a notification as read. DEVELOPER and others are restricted.");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification", description = "Deletes a specific notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN can delete a notification. CLIENT, PROVIDER, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN can delete a notification. CLIENT, PROVIDER, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @PostMapping("/booking-request/{bookingId}")
    @Operation(summary = "Send booking request notification", description = "Sends a notification to a provider about a new booking request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN can send booking request notifications. CLIENT, PROVIDER, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendBookingRequestNotification(@PathVariable Long bookingId) {
        try {
            notificationService.sendBookingRequestNotification(bookingId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN can send booking request notifications. CLIENT, PROVIDER, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @PostMapping("/booking-confirmation/{bookingId}")
    @Operation(summary = "Send booking confirmation notification", description = "Sends a notification to a client about a confirmed booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN can send booking confirmation notifications. CLIENT, PROVIDER, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendBookingConfirmationNotification(@PathVariable Long bookingId) {
        try {
            notificationService.sendBookingConfirmationNotification(bookingId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN can send booking confirmation notifications. CLIENT, PROVIDER, DRIVER, DEVELOPER, and others are restricted.");
        }
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