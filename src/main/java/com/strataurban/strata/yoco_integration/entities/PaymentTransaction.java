package com.strataurban.strata.yoco_integration.entities;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.C;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_checkout_id", columnList = "checkoutId"),
        @Index(name = "idx_external_ref", columnList = "externalReference"),
        @Index(name = "idx_idempotency_key", columnList = "idempotencyKey"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_customer_id", columnList = "customerId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String checkoutId;

    @Column(unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    @Column(length = 100)
    private String paymentId;

    @Column(nullable = false, length = 50)
    private String externalReference;

    @Column(nullable = false, length = 100)
    private Long customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(length = 20)
    private String processingMode; // 'test' or 'live'

    @Column(length = 500)
    private String redirectUrl;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(length = 50)
    private String errorCode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime completedAt;

    @Version
    private Long version; // Optimistic locking

    @Column
    private Long recipientId;

    @Column
    private Long bookingId;

    public enum PaymentStatus {
        CREATED,           // Checkout created, awaiting payment
        PENDING,           // Payment initiated by customer
        PROCESSING,        // Payment being processed
        SUCCEEDED,         // Payment successful
        FAILED,            // Payment failed
        CANCELLED,         // Payment cancelled
        EXPIRED,           // Checkout expired
        REFUNDED,          // Full refund
        PARTIALLY_REFUNDED // Partial refund
    }
}
