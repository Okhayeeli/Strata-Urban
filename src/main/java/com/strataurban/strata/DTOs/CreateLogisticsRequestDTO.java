package com.strataurban.strata.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CreateLogisticsRequestDTO {
    @NotNull
    private Long providerId;
    @NotNull private Long routeId;
    @NotNull private Long serviceTypeId;

    private Integer numberOfPeople;
    private List<String> supplyItems;
    private List<String> giftTypes;
    private List<String> instrumentTypes;
    private Double estimatedWeight;
    private String sizeCategory;

    private LocalDateTime pickupDateTime;
    private LocalDateTime dropOffDateTime;
    private boolean hasMultipleStops;
    private boolean returnTripRequired;
    private String additionalInfo;
}