package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for accepting an offer")
public class AcceptOfferRequest {

    @Schema(description = "Unique identifier of the offer to accept", example = "456")
    private Long offerId;
}