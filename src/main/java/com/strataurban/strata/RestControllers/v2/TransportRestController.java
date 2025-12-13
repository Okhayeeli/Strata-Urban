package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.RequestBodyIdDto;
import com.strataurban.strata.DTOs.v2.TransportDTO;
import com.strataurban.strata.DTOs.v2.TransportStatusRequest;
import com.strataurban.strata.Entities.Providers.Transport;
import com.strataurban.strata.Security.LoggedUser;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.Services.v2.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/transports")
@Tag(name = "Transport Management", description = "APIs for managing transports (vehicles) owned by providers")
public class TransportRestController {

    private final TransportService transportService;

    public TransportRestController(TransportService transportService) {
        this.transportService = transportService;
    }

    @PostMapping
    @Operation(summary = "Add a new transport", description = "Allows a PROVIDER to add a new transport to their fleet, or ADMIN/CUSTOMER_SERVICE to add for any provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transport data"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER, ADMIN, or CUSTOMER_SERVICE allowed")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<TransportDTO> addTransport(@RequestBody Transport transport, @LoggedUser SecurityUserDetails userDetails) {
        Transport savedTransport = transportService.addTransport(transport, userDetails);
        return ResponseEntity.ok(mapToDTO(savedTransport));
    }

    @GetMapping
    @Operation(summary = "Get all transports for the authenticated provider", description = "Fetches all transports owned by the authenticated PROVIDER, or all transports for ADMIN/CUSTOMER_SERVICE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transports retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER, ADMIN, or CUSTOMER_SERVICE allowed")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<List<TransportDTO>> getProviderTransports(@LoggedUser SecurityUserDetails userDetails) {
        List<Transport> transports = transportService.getProviderTransports(userDetails);
        List<TransportDTO> transportDTOs = transports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transportDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transport details by ID", description = "Fetches details of a specific transport, restricted to the PROVIDER owning it or ADMIN/CUSTOMER_SERVICE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER owning the transport, ADMIN, or CUSTOMER_SERVICE allowed"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    @PreAuthorize("(hasRole('PROVIDER') and @methodSecurity.isTransportProviderOwner(#id, principal.id)) or hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<TransportDTO> getTransportById(@PathVariable Long id) {
        Transport transport = transportService.getTransportById(id);
        return ResponseEntity.ok(mapToDTO(transport));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transport details", description = "Updates a specific transport, restricted to the PROVIDER owning it or ADMIN/CUSTOMER_SERVICE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transport data"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER owning the transport, ADMIN, or CUSTOMER_SERVICE allowed"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    @PreAuthorize("(hasRole('PROVIDER') and @methodSecurity.isTransportProviderOwner(#id, principal.id)) or hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<TransportDTO> updateTransport(@PathVariable Long id, @RequestBody Transport transport, @LoggedUser SecurityUserDetails userDetails) throws IllegalAccessException {
        Transport updatedTransport = transportService.updateTransport(id, transport, userDetails);
        return ResponseEntity.ok(mapToDTO(updatedTransport));
    }

    @DeleteMapping()
    @Operation(summary = "Delete a transport", description = "Deletes a specific transport, restricted to the PROVIDER owning it or ADMIN/CUSTOMER_SERVICE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER owning the transport, ADMIN, or CUSTOMER_SERVICE allowed"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<Void> deleteTransport(@RequestBody RequestBodyIdDto requestBodyIdDto) {
        transportService.deleteTransport(requestBodyIdDto.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available")
    @Operation(summary = "Get available transports", description = "Fetches available transports for a booking, restricted to the authenticated PROVIDER's fleet or all transports for ADMIN/CUSTOMER_SERVICE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available transports retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category or capacity"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER, ADMIN, or CUSTOMER_SERVICE allowed")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<List<TransportDTO>> getAvailableTransports(
            @RequestParam(required = false) String transportCategory,
            @RequestParam(required = false) Integer capacity,
            @LoggedUser SecurityUserDetails userDetails) {
        List<Transport> transports = transportService.getAvailableTransports(userDetails, transportCategory, capacity);
        List<TransportDTO> transportDTOs = transports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transportDTOs);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update transport status", description = "Updates the status of a specific transport, restricted to the PROVIDER owning it or ADMIN/CUSTOMER_SERVICE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transport status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER owning the transport, ADMIN, or CUSTOMER_SERVICE allowed"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    @PreAuthorize("(hasRole('PROVIDER') and @methodSecurity.isTransportProviderOwner(#id, principal.id)) or hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<TransportDTO> updateTransportStatus(
            @PathVariable Long id,
            @RequestBody TransportStatusRequest statusRequest) {
        Transport updatedTransport = transportService.updateTransportStatus(id, statusRequest.getStatus());
        return ResponseEntity.ok(mapToDTO(updatedTransport));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<Page<Transport>> getTransportsByFilters(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<Transport> transports = transportService.findTransportsByFilters(
                providerId, type, capacity, description, plateNumber, brand, model,
                color, state, company, routeId, status, pageable);
        return ResponseEntity.ok(transports);
    }

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