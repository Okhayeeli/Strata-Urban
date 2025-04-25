package com.strataurban.strata.DTOs.v2;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for creating an offer")
public class CreateOfferRequest {

    @Schema(description = "Unique identifier of the provider making the offer", example = "123")
    private Long providerId;

    @Schema(description = "Price offered by the provider", example = "150.00")
    private Double price;

    @Schema(description = "Additional notes or terms from the provider", example = "Available after 2 PM")
    private String notes;
}