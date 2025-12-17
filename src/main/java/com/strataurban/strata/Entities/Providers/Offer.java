package com.strataurban.strata.Entities.Providers;

import com.strataurban.strata.Enums.OfferStatus;
import jakarta.persistence.*;
import lombok.*;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Offer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long bookingRequestId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column
    private Double discountPercentage; // Optional discount (e.g., 10.0 for 10%)

    @Column(nullable = false)
    private BigDecimal discountedPrice;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Column(length = 500)
    private String notes;

    @Column
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private OfferStatus status;

    @Column
    private LocalDateTime validUntil; // Optional expiration date for the offer

    @Column
    private String websiteLink; // Optional link to provider's website

    @Column
    private String estimatedDuration; // Optional estimated service duration (e.g., "2 hours")

    @Column(length = 500)
    private String specialConditions; // Optional special terms or conditions

    @Column(length = 500, nullable = false, updatable = false)
    private String transactionReference = UUID.randomUUID().toString();

    @Column
    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }


    @PreUpdate
    protected void onUpdate() {
        calculateDiscountedPrice();
    }

    /**
     * Calculates the discounted price based on the price and discount percentage.
     * If discount is null, zero, or empty, the discounted price equals the actual price.
     */
    private void calculateDiscountedPrice() {
        if (price == null) {
            this.discountedPrice = BigDecimal.ZERO;
            return;
        }

        if (discountPercentage == null || discountPercentage <= 0) {
            this.discountedPrice = price;
        } else {
            BigDecimal discount = price.multiply(BigDecimal.valueOf(discountPercentage / 100));
            this.discountedPrice = price.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Custom setter for price that triggers discounted price calculation.
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
        calculateDiscountedPrice();
    }

    /**
     * Custom setter for discount percentage that triggers discounted price calculation.
     */
    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
        calculateDiscountedPrice();
    }

    /**
     * Returns the effective price to be used.
     * If price and discounted price are equal, returns the price.
     * Otherwise, returns the discounted price.
     */
    public BigDecimal getEffectivePrice() {
        if (price != null && discountedPrice != null && price.compareTo(discountedPrice) == 0) {
            return price;
        }
        return discountedPrice != null ? discountedPrice : price;
    }


    /**
     * Returns a formatted price string that includes discount information if applicable.
     * Examples:
     * - No discount: "100.00 ZAR"
     * - With discount: "90.00 USD (10% off from 100.00 ZAR)"
     */
    public String getFormattedPriceWithDiscount() {
        if (price == null) {
            return "N/A";
        }

        String currency = currencyCode != null ? currencyCode : "ZAR";

        // If there's no discount or discount is zero
        if (discountPercentage == null || discountPercentage <= 0 ||
                price.compareTo(discountedPrice) == 0) {
            return String.format("%s %s", price, currency);
        }

        // If there's a discount
        return String.format("%s %s (%s%% off from %s %s)",
                discountedPrice,
                currency,
                String.format("%.0f", discountPercentage),
                price,
                currency
        );
    }
}