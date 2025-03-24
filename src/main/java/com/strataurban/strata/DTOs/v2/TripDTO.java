package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a Trip")
public class TripDTO {

    @Schema(description = "Unique identifier of the trip", example = "1")
    private Long id;

    @Schema(description = "ID of the associated booking", example = "2")
    private Long bookingId;

    @Schema(description = "ID of the provider handling the trip", example = "3")
    private Long providerId;

    @Schema(description = "ID of the client who booked the trip", example = "4")
    private Long clientId;

    @Schema(description = "ID of the driver assigned to the trip", example = "5")
    private Long driverId;

    @Schema(description = "ID of the vehicle used for the trip", example = "6")
    private Long vehicleId;

    @Schema(description = "Status of the trip", example = "IN_PROGRESS")
    private BookingStatus status;

    @Schema(description = "Start time of the trip", example = "2024-03-15T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "End time of the trip", example = "2024-03-15T11:00:00")
    private LocalDateTime endTime;

    @Schema(description = "Comma-separated list of additional stops for the trip", example = "Stop 1,Stop 2")
    private String additionalStops;
}