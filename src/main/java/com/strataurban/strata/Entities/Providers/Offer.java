package com.strataurban.strata.Entities.Providers;

import com.strataurban.strata.Enums.OfferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers")
@Data
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
    private Double price;

    @Column(length = 500)
    private String notes;

    @Column
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private OfferStatus status;

    @Column
    private LocalDateTime validUntil; // Optional expiration date for the offer

    @Column
    private Double discountPercentage; // Optional discount (e.g., 10.0 for 10%)

    @Column
    private String websiteLink; // Optional link to provider's website

    @Column
    private String estimatedDuration; // Optional estimated service duration (e.g., "2 hours")

    @Column(length = 500)
    private String specialConditions; // Optional special terms or conditions

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}