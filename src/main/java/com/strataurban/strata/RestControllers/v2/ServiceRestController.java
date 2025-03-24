package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.ServiceDTO;
import com.strataurban.strata.Entities.Providers.Services;
import com.strataurban.strata.Services.v2.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/services")
@Tag(name = "Service Management", description = "APIs for managing services offered by providers")
public class ServiceRestController {

    private final ServiceService serviceService;

    @Autowired
    public ServiceRestController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    @Operation(summary = "Add a new service", description = "Allows a provider to add a new service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid service data")
    })
    public ResponseEntity<ServiceDTO> addService(@RequestBody Services service) {
        Services savedService = serviceService.addService(service);
        return ResponseEntity.ok(mapToDTO(savedService));
    }

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "Get all services for a provider", description = "Fetches all services offered by a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<List<ServiceDTO>> getProviderServices(@PathVariable Long providerId) {
        List<Services> services = serviceService.getProviderServices(providerId);
        List<ServiceDTO> serviceDTOs = services.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(serviceDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service details by ID", description = "Fetches details of a specific service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Long id) {
        Services service = serviceService.getServiceById(id);
        return ResponseEntity.ok(mapToDTO(service));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update service details", description = "Updates the details of a specific service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service updated successfully"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    public ResponseEntity<ServiceDTO> updateService(
            @PathVariable Long id,
            @RequestBody Services service) {
        Services updatedService = serviceService.updateService(id, service);
        return ResponseEntity.ok(mapToDTO(updatedService));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service", description = "Deletes a specific service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get all available services", description = "Fetches all services across providers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully")
    })
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        List<Services> services = serviceService.getAllServices();
        List<ServiceDTO> serviceDTOs = services.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(serviceDTOs);
    }

    // Helper method to map Services entity to ServiceDTO
    private ServiceDTO mapToDTO(Services service) {
        return new ServiceDTO(
                service.getId(),
                service.getServiceName(),
                service.getProviderId()
        );
    }
}