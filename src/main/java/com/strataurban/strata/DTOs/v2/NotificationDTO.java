package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.NotificationType;
import com.strataurban.strata.Enums.RecipientType;
import com.strataurban.strata.Enums.ReferenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a Notification")
public class NotificationDTO {

    @Schema(description = "Unique identifier of the notification", example = "1")
    private Long id;

    @Schema(description = "ID of the user receiving the notification", example = "1")
    private Long recipientId;

    @Schema(description = "Type of recipient (CLIENT, PROVIDER, DRIVER, ADMIN)", example = "PROVIDER")
    private RecipientType recipientType;

    @Schema(description = "Type of notification (e.g., BOOKING_REQUEST, BOOKING_CONFIRMED)", example = "BOOKING_REQUEST")
    private NotificationType type;

    @Schema(description = "Message content of the notification", example = "A new booking request has been received")
    private String message;

    @Schema(description = "ID of the related entity (e.g., booking ID)", example = "2")
    private Long referenceId;

    @Schema(description = "Type of reference (e.g., BOOKING, TRIP)", example = "BOOKING")
    private ReferenceType referenceType;

    @Schema(description = "Whether the notification has been read", example = "false")
    private boolean isRead;

    @Schema(description = "Timestamp when the notification was created", example = "2024-03-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the notification was read", example = "2024-03-15T10:05:00")
    private LocalDateTime readAt;

    @Schema(description = "Additional metadata for the notification", example = "{\"url\": \"/bookings/2\"}")
    private String metadata;
}