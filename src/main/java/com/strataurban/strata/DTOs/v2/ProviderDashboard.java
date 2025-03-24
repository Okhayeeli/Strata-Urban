package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for Provider Dashboard Statistics")
public class ProviderDashboard {

    @Schema(description = "Number of bookings for today", example = "8")
    private int todaysBookings;

    @Schema(description = "Number of pending confirmations", example = "4")
    private int pendingConfirmations;

    @Schema(description = "Number of vehicles assigned", example = "12")
    private int vehiclesAssigned;

    @Schema(description = "Revenue for the current month", example = "14250")
    private double monthlyRevenue;
}