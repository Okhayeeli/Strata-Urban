package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.TransportDTO;
import com.strataurban.strata.DTOs.v2.TransportStatusRequest;
import com.strataurban.strata.Entities.Providers.Transport;
import com.strataurban.strata.Services.v2.TransportService;
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
@RequestMapping("/api/v2/transports")
@Tag(name = "Transport Management", description = "APIs for managing transports (vehicles) owned by providers")
public class TransportRestController {

    private final TransportService transportService;

    @Autowired
    public TransportRestController(TransportService transportService) {
        this.transportService = transportService;
    }

    @PostMapping
    @Operation(summary = "Add a new transport", description = "Allows a provider to add a new transport to their fleet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transport data")
    })
    public ResponseEntity<TransportDTO> addTransport(@RequestBody Transport transport) {
        Transport savedTransport = transportService.addTransport(transport);
        return ResponseEntity.ok(mapToDTO(savedTransport));
    }

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "Get all transports for a provider", description = "Fetches all transports owned by a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transports retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<List<TransportDTO>> getProviderTransports(@PathVariable Long providerId) {
        List<Transport> transports = transportService.getProviderTransports(providerId);
        List<TransportDTO> transportDTOs = transports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transportDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transport details by ID", description = "Fetches details of a specific transport")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    public ResponseEntity<TransportDTO> getTransportById(@PathVariable Long id) {
        Transport transport = transportService.getTransportById(id);
        return ResponseEntity.ok(mapToDTO(transport));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transport details", description = "Updates the details of a specific transport")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport updated successfully"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    public ResponseEntity<TransportDTO> updateTransport(
            @PathVariable Long id,
            @RequestBody Transport transport) {
        Transport updatedTransport = transportService.updateTransport(id, transport);
        return ResponseEntity.ok(mapToDTO(updatedTransport));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transport", description = "Deletes a specific transport from the provider's fleet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    public ResponseEntity<Void> deleteTransport(@PathVariable Long id) {
        transportService.deleteTransport(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available")
    @Operation(summary = "Get available transports", description = "Fetches available transports for a booking based on provider, category, and capacity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available transports retrieved successfully")
    })
    public ResponseEntity<List<TransportDTO>> getAvailableTransports(
            @RequestParam Long providerId,
            @RequestParam String transportCategory,
            @RequestParam Integer capacity) {
        List<Transport> transports = transportService.getAvailableTransports(providerId, transportCategory, capacity);
        List<TransportDTO> transportDTOs = transports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transportDTOs);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update transport status", description = "Updates the status of a specific transport (e.g., Available, Booked)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Transport not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    public ResponseEntity<TransportDTO> updateTransportStatus(
            @PathVariable Long id,
            @RequestBody TransportStatusRequest statusRequest) {
        Transport updatedTransport = transportService.updateTransportStatus(id, statusRequest.getStatus());
        return ResponseEntity.ok(mapToDTO(updatedTransport));
    }

    // Helper method to map Transport entity to TransportDTO
    private TransportDTO mapToDTO(Transport transport) {
        return new TransportDTO(
                transport.getId(),
                transport.getProviderId(),
                transport.getType(),
                transport.getCapacity(),
                transport.getDescription(),
                transport.getPlateNumber(),
                transport.getBrand(),
                transport.getModel(),
                transport.getColor(),
                transport.getState(),
                transport.getCompany(),
                transport.getRouteId(),
                transport.getStatus()
        );
    }
}