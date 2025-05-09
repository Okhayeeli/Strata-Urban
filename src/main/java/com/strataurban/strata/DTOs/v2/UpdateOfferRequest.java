package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.OfferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateOfferRequest {

    @Schema(description = "Unique identifier of the provider making the offer", example = "123")
    private Long providerId;

    @Schema(description = "Price offered by the provider", example = "150.00")
    private Double price;

    @Schema(description = "Additional notes or terms from the provider", example = "Available after 2 PM")
    private String notes;

    @Schema(description = "Booking request ID to which this offer is responding", example = "456")
    private Long bookingRequestId;

    @Schema(description = "Offer creation timestamp", example = "2025-05-09T15:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "Status of the offer", example = "PENDING")
    private OfferStatus status;

    @Schema(description = "The date and time until which the offer is valid", example = "2025-05-10T23:59:59")
    private LocalDateTime validUntil;

    @Schema(description = "Discount percentage on the offer", example = "10.0")
    private Double discountPercentage;

    @Schema(description = "Provider's website link for more information", example = "https://provider.com/offer")
    private String websiteLink;

    @Schema(description = "Estimated duration for completing the service", example = "3 days")
    private String estimatedDuration;

    @Schema(description = "Special conditions or terms for the offer", example = "Requires a 50% deposit")
    private String specialConditions;
}