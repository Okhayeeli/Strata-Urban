package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a Transport (Vehicle)")
public class TransportDTO {

    @Schema(description = "Unique identifier of the transport", example = "1")
    private Long id;

    @Schema(description = "ID of the provider owning the transport", example = "2")
    private Long providerId;

    @Schema(description = "Type of the transport", example = "Bus")
    private String type;

    @Schema(description = "Capacity of the transport", example = "15")
    private int capacity;

    @Schema(description = "Description of the transport", example = "Luxury bus with AC")
    private String description;

    @Schema(description = "Plate number of the transport", example = "ABC123")
    private String plateNumber;

    @Schema(description = "Brand of the transport", example = "Mercedes")
    private String brand;

    @Schema(description = "Model of the transport", example = "Sprinter")
    private String model;

    @Schema(description = "Color of the transport", example = "White")
    private String color;

    @Schema(description = "State of the transport", example = "CA")
    private String state;

    @Schema(description = "Company name associated with the transport", example = "TechGlobal Inc.")
    private String company;

    @Schema(description = "ID of the route associated with the transport", example = "3")
    private Long routeId;

    @Schema(description = "Status of the transport", example = "Available")
    private String status;
}