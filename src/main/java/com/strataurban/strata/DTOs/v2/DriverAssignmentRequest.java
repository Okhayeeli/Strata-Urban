package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to assign a driver to a booking")
public class DriverAssignmentRequest {

    @Schema(description = "ID of the driver to assign", example = "5")
    private Long driverId;

    @Schema(description = "ID of the vehicle to assign", example = "3")
    private Long vehicleId;
}