package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.ServiceAreaReportDTO;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ServiceArea;
import com.strataurban.strata.Services.v2.ServiceAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/service-areas")
@Tag(name = "Service Area Management", description = "APIs for managing service areas and their association with providers")
public class ServiceAreaRestController {

    private final ServiceAreaService serviceAreaService;

    @Autowired
    public ServiceAreaRestController(ServiceAreaService serviceAreaService) {
        this.serviceAreaService = serviceAreaService;
    }

    @PostMapping
    @Operation(summary = "Create a new service area", description = "Creates a new service area")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service area created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid service area data")
    })
    public ResponseEntity<ServiceArea> createServiceArea(@RequestBody ServiceArea serviceArea) {
        return ResponseEntity.ok(serviceAreaService.createServiceArea(serviceArea));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a service area by ID", description = "Fetches a specific service area")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service area retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Service area not found")
    })
    public ResponseEntity<ServiceArea> getServiceAreaById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceAreaService.getServiceAreaById(id));
    }

    @GetMapping
    @Operation(summary = "Get all service areas", description = "Fetches all service areas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service areas retrieved successfully")
    })
    public ResponseEntity<List<ServiceArea>> getAllServiceAreas() {
        return ResponseEntity.ok(serviceAreaService.getAllServiceAreas());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service area", description = "Updates a specific service area")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service area updated successfully"),
            @ApiResponse(responseCode = "404", description = "Service area not found")
    })
    public ResponseEntity<ServiceArea> updateServiceArea(
            @PathVariable Long id,
            @RequestBody ServiceArea serviceArea) {
        return ResponseEntity.ok(serviceAreaService.updateServiceArea(id, serviceArea));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service area", description = "Deletes a specific service area")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service area deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Service area not found")
    })
    public ResponseEntity<Void> deleteServiceArea(@PathVariable Long id) {
        serviceAreaService.deleteServiceArea(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "Get service areas for a provider", description = "Fetches the list of service areas for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service areas retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<List<String>> getServiceAreasForProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(serviceAreaService.getServiceAreasForProvider(providerId));
    }

    @PostMapping("/provider/{providerId}/add")
    @Operation(summary = "Add service areas to a provider", description = "Adds service areas to a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service areas added successfully"),
            @ApiResponse(responseCode = "404", description = "Provider or service area not found")
    })
    public ResponseEntity<String> addServiceAreasToProvider(
            @PathVariable Long providerId,
            @RequestBody List<Long> serviceAreaIds) {
        serviceAreaService.addServiceAreasToProvider(providerId, serviceAreaIds);
        return ResponseEntity.ok("Successfully added Service Areas");
    }

    @PostMapping("/provider/{providerId}/remove")
    @Operation(summary = "Remove service areas from a provider", description = "Removes service areas from a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service areas removed successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<String> removeServiceAreasFromProvider(
            @PathVariable Long providerId,
            @RequestBody List<Long> serviceAreaIds) {
        serviceAreaService.removeServiceAreasFromProvider(providerId, serviceAreaIds);
        return ResponseEntity.ok("Successfully removed service areas");
    }

    @GetMapping("/{serviceAreaId}/providers")
    @Operation(summary = "Get providers in a service area", description = "Fetches all providers operating in a specific service area")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Service area not found")
    })
    public ResponseEntity<List<Provider>> getProvidersInServiceArea(@PathVariable Long serviceAreaId) {
        return ResponseEntity.ok(serviceAreaService.getProvidersInServiceArea(serviceAreaId));
    }

    @GetMapping("/report")
    @Operation(summary = "Get service area report", description = "Generates a report showing the number of providers and the list of providers in each service area")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully")
    })
    public ResponseEntity<List<ServiceAreaReportDTO>> getServiceAreaReport() {
        return ResponseEntity.ok(serviceAreaService.getServiceAreaReport());
    }
}