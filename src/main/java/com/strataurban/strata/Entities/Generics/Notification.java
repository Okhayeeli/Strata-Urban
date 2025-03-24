package com.strataurban.strata.Entities.Generics;

import com.strataurban.strata.Enums.NotificationType;
import com.strataurban.strata.Enums.RecipientType;
import com.strataurban.strata.Enums.ReferenceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who receives the notification (Client, Provider, or Driver)
    @Column(nullable = false)
    private Long recipientId;

    // The type of recipient (e.g., CLIENT, PROVIDER, DRIVER)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecipientType recipientType;

    // The type of notification (e.g., BOOKING_REQUEST, BOOKING_CONFIRMED, TRIP_STARTED)
    @Column(nullable = false, name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    // The message content of the notification
    @Column(nullable = false)
    private String message;

    // Reference to the related entity (e.g., booking ID, trip ID)
    @Column
    private Long referenceId;

    // The type of reference (e.g., BOOKING, TRIP)
    @Column
    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;

    // Whether the notification has been read
    @Column(nullable = false)
    private boolean isRead = false;

    // Timestamp when the notification was created
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Optional: Timestamp when the notification was read
    @Column
    private LocalDateTime readAt;

    // Optional: Additional metadata (e.g., for custom actions or links)
    @Column
    private String metadata;
}

