package com.strataurban.strata.yoco_integration.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_events", indexes = {
        @Index(name = "idx_event_id", columnList = "eventId"),
        @Index(name = "idx_checkout_id", columnList = "checkoutId"),
        @Index(name = "idx_event_type", columnList = "eventType"),
        @Index(name = "idx_processed", columnList = "processed")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String checkoutId;

    @Column(nullable = false, length = 50)
    private String eventType; // e.g., 'payment.succeeded'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload; // Store complete webhook payload

    @Column(nullable = false)
    private Boolean processed;

    @Column
    private Boolean signatureValid;

    @Column(columnDefinition = "TEXT")
    private String processingError;

    @Column
    private Integer retryCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @Column
    private LocalDateTime processedAt;
}
