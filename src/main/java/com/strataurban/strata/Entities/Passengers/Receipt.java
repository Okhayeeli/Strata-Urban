package com.strataurban.strata.Entities.Passengers;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts", indexes = {
        @Index(name = "idx_receipt_number", columnList = "receiptNumber"),
        @Index(name = "idx_payment_transaction_id", columnList = "paymentTransactionId"),
        @Index(name = "idx_booking_id", columnList = "bookingId"),
        @Index(name = "idx_client_id", columnList = "clientId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String receiptNumber; // e.g., RCP-20250117-XXXXX

    // ============ PRIMARY PAYMENT DETAILS ============
    @Column(nullable = false)
    private Long paymentTransactionId;

    @Column(nullable = false, length = 100)
    private String checkoutId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid;

    @Column(length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(length = 50)
    private String paymentMethod; // Card, Bank Transfer, etc.

    // ============ BOOKING DETAILS ============
    @Column(nullable = false)
    private Long bookingId;

    @Column(length = 100)
    private String bookingReference;

    @Column(nullable = false)
    private Long offerId;

    @Column(length = 100)
    private String offerTransactionReference;

    // ============ CLIENT DETAILS ============
    @Column(nullable = false)
    private Long clientId;

    @Column(length = 200)
    private String clientName; // firstName + lastName

    @Column(length = 200)
    private String clientEmail;

    @Column(length = 50)
    private String clientPhone;

    // ============ PROVIDER DETAILS ============
    @Column(nullable = false)
    private Long providerId;

    @Column(length = 200)
    private String providerName; // companyName or firstName + lastName

    @Column(length = 200)
    private String providerEmail;

    @Column(length = 50)
    private String providerPhone;

    @Column(length = 500)
    private String providerAddress;

    // ============ SERVICE DETAILS ============
    @Column(length = 500)
    private String serviceDescription; // Brief description of service type

    @Column
    private LocalDateTime serviceDate;

    @Column(length = 500)
    private String pickUpLocation;

    @Column(length = 500)
    private String destination;

    @Column(length = 100)
    private String routeDescription; // start -> end

    // ============ VEHICLE & DRIVER DETAILS ============
    @Column
    private Long vehicleId;

    @Column(length = 100)
    private String vehicleDetails; // brand, model, plateNumber

    @Column
    private Long driverId;

    @Column(length = 200)
    private String driverName;

    // ============ PRICING BREAKDOWN ============
    @Column(precision = 19, scale = 2)
    private BigDecimal originalPrice;

    @Column(precision = 5)
    private Double discountPercentage;

    @Column(precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal serviceFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    // ============ ADDITIONAL BOOKING INFO ============
    @Column
    private Integer numberOfPassengers;

    @Column
    private Boolean hasMultipleStops;

    @Column
    private Boolean isReturnTrip;

    @Column(length = 1000)
    private String additionalNotes;

    // ============ METADATA ============
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(length = 50)
    private String status; // ISSUED, VOIDED, REFUNDED

    @Column(length = 1000)
    private String specialConditions; // From offer

    @Column(length = 500)
    private String notes; // Any additional notes

    @PrePersist
    protected void onCreate() {
        if (this.receiptNumber == null) {
            this.receiptNumber = generateReceiptNumber();
        }
        if (this.status == null) {
            this.status = "ISSUED";
        }
    }

    private String generateReceiptNumber() {
        // Format: RCP-YYYYMMDD-XXXXXX (where X is random/sequential)
        String datePart = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
        );
        String randomPart = String.format("%06d", (int)(Math.random() * 10000));
        return "STU-RCP-" + datePart + "-" + randomPart;
    }
}