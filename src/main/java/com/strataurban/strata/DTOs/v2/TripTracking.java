package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for Trip Tracking Information")
public class TripTracking {

    @Schema(description = "Current latitude of the vehicle", example = "40.7128")
    private Double currentLatitude;

    @Schema(description = "Current longitude of the vehicle", example = "-74.0060")
    private Double currentLongitude;

    @Schema(description = "Estimated time of arrival (ETA) in minutes", example = "15")
    private Integer etaMinutes;

    @Schema(description = "Current status of the trip", example = "IN_PROGRESS")
    private BookingStatus status;
}