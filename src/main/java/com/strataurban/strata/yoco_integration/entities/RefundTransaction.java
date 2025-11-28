package com.strataurban.strata.yoco_integration.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_transactions", indexes = {
        @Index(name = "idx_refund_id", columnList = "refundId"),
        @Index(name = "idx_payment_transaction_id", columnList = "paymentTransactionId"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String refundId;

    @Column(nullable = false)
    private Long paymentTransactionId;

    @Column(nullable = false, length = 100)
    private String checkoutId;

    @Column(nullable = false, length = 100)
    private String paymentId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal refundAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus status;

    @Column(length = 500)
    private String reason;

    @Column(length = 100)
    private String initiatedBy;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    public enum RefundStatus {
        PENDING,
        SUCCEEDED,
        FAILED
    }
}