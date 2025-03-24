package com.strataurban.strata.Entities.Providers;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "service_area")
@Schema(description = "Entity representing a service area where providers operate")
public class ServiceArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the service area", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Name of the service area", example = "Downtown")
    private String name;

    @Column
    @Schema(description = "Description of the service area", example = "Central business district")
    private String description;

}