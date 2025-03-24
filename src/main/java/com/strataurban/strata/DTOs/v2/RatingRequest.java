package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to rate a provider")
public class RatingRequest {

    @Schema(description = "Rating value (1 to 5)", example = "4.5")
    private Double rating;

    @Schema(description = "Optional comment for the rating", example = "Great service!")
    private String comment;
}