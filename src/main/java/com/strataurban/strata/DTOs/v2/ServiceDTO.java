package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a Service")
public class ServiceDTO {

    @Schema(description = "Unique identifier of the service", example = "1")
    private Long id;

    @Schema(description = "Name of the service", example = "Transport")
    private String serviceName;

    @Schema(description = "ID of the provider offering the service", example = "2")
    private Long providerId;
}