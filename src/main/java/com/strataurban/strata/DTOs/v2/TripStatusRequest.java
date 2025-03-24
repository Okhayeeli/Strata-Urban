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
@Schema(description = "Request to update the status of a trip")
public class TripStatusRequest {

    @Schema(description = "New status of the trip", example = "IN_PROGRESS")
    private BookingStatus status;
}