package com.strataurban.strata.DTOs.v2;


import com.strataurban.strata.Entities.Providers.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for the service area report, showing the number of providers and the list of providers in each service area")
public class ServiceAreaReportDTO {

    @Schema(description = "ID of the service area", example = "1")
    private Long serviceAreaId;

    @Schema(description = "Name of the service area", example = "Downtown")
    private String serviceAreaName;

    @Schema(description = "Number of providers in the service area", example = "5")
    private int providerCount;

    @Schema(description = "List of providers in the service area")
    private List<Provider> providers;
}