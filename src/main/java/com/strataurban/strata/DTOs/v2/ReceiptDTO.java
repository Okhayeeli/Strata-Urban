package com.strataurban.strata.DTOs.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for receipt responses - includes all essential information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDTO {

    // Receipt identification
    private Long id;
    private String receiptNumber;
    private String status;
    private LocalDateTime generatedAt;

    // Payment summary
    private PaymentSummary payment;

    // Booking summary
    private BookingSummary booking;

    // Client information
    private PartyDetails client;

    // Provider information
    private PartyDetails provider;

    // Service details
    private ServiceDetails service;

    // Pricing breakdown
    private PricingBreakdown pricing;

    // Additional information
    private AdditionalInfo additionalInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummary {
        private Long paymentTransactionId;
        private String checkoutId;
        private BigDecimal amountPaid;
        private String currency;
        private LocalDateTime paymentDate;
        private String paymentMethod;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingSummary {
        private Long bookingId;
        private String bookingReference;
        private Long offerId;
        private String offerTransactionReference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyDetails {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceDetails {
        private String description;
        private LocalDateTime serviceDate;
        private String pickUpLocation;
        private String destination;
        private String routeDescription;
        private VehicleInfo vehicle;
        private DriverInfo driver;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private Long vehicleId;
        private String details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverInfo {
        private Long driverId;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingBreakdown {
        private BigDecimal originalPrice;
        private Double discountPercentage;
        private BigDecimal discountAmount;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal serviceFee;
        private BigDecimal totalAmount;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalInfo {
        private Integer numberOfPassengers;
        private Boolean hasMultipleStops;
        private Boolean isReturnTrip;
        private String additionalNotes;
        private String specialConditions;
        private String notes;
    }
}
