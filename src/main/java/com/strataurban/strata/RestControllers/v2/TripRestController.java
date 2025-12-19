package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.AdditionalStopsRequest;
import com.strataurban.strata.DTOs.v2.TripDTO;
import com.strataurban.strata.DTOs.v2.TripStatusRequest;
import com.strataurban.strata.DTOs.v2.TripTracking;
import com.strataurban.strata.Entities.RequestEntities.Trips;
import com.strataurban.strata.Services.v2.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/trips")
@Tag(name = "Trip Management", description = "APIs for managing trips")
public class TripRestController {

    private final TripService tripService;

    @Autowired
    public TripRestController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/{bookingId}/start")
    @Operation(summary = "Start a trip", description = "Starts a trip for a specific booking (Provider/Driver action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid booking state"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER/DRIVER or ADMIN can start a trip. DEVELOPER is restricted from this action, and CUSTOMER_SERVICE, CLIENT, and others are not allowed.")
    })
    @PreAuthorize("(hasRole('PROVIDER') or hasRole('ADMIN'))")
    public ResponseEntity<TripDTO> startTrip(@PathVariable Long bookingId) {
        try {
            Trips trip = tripService.startTrip(bookingId);
            return ResponseEntity.ok(mapToDTO(trip));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER/DRIVER or ADMIN can start a trip. DEVELOPER is restricted from this action, and CUSTOMER_SERVICE, CLIENT, and others are not allowed.");
        }
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "End a trip", description = "Ends a specific trip (Provider/Driver action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip ended successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid trip state"),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER/DRIVER or ADMIN can end a trip. DEVELOPER is restricted from this action, and CUSTOMER_SERVICE, CLIENT, and others are not allowed.")
    })
    @PreAuthorize("(hasRole('PROVIDER') or hasRole('ADMIN'))")
    public ResponseEntity<TripDTO> endTrip(@PathVariable Long id) {
        try {
            Trips trip = tripService.endTrip(id);
            return ResponseEntity.ok(mapToDTO(trip));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER/DRIVER or ADMIN can end a trip. DEVELOPER is restricted from this action, and CUSTOMER_SERVICE, CLIENT, and others are not allowed.");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip details by ID", description = "Fetches details of a specific trip")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN, DEVELOPER, CUSTOMER_SERVICE, PROVIDER (if authorized), or CLIENT (if authorized) can access this endpoint. Others are restricted.")
    })
    @PreAuthorize("(hasRole('ADMIN') or hasRole('DEVELOPER') or hasRole('CUSTOMER_SERVICE') or (hasRole('PROVIDER') and @tripService.isAuthorizedProviderTrip(#id, principal.id)) or (hasRole('CLIENT') and @tripService.isAuthorizedClientTrip(#id, principal.id)))")
    public ResponseEntity<TripDTO> getTripById(@PathVariable Long id) {
        try {
            Trips trip = tripService.getTripById(id);
            return ResponseEntity.ok(mapToDTO(trip));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN, DEVELOPER, CUSTOMER_SERVICE, PROVIDER (if authorized), or CLIENT (if authorized) can access this endpoint. Others are restricted.");
        }
    }

    @GetMapping("/{id}/track")
    @Operation(summary = "Track a trip", description = "Tracks a specific trip in real-time (Client action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip tracking information retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Trip not in progress"),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT (if authorized) or ADMIN can track a trip. DEVELOPER, PROVIDER, CUSTOMER_SERVICE, and others are restricted.")
    })
    @PreAuthorize("(hasRole('CLIENT') and @tripService.isAuthorizedClientTrip(#id, principal.id)) or hasRole('ADMIN')")
    public ResponseEntity<TripTracking> trackTrip(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tripService.trackTrip(id));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT (if authorized) or ADMIN can track a trip. DEVELOPER, PROVIDER, CUSTOMER_SERVICE, and others are restricted.");
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update trip status", description = "Updates the status of a specific trip (e.g., In Progress, Completed)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER/DRIVER or ADMIN can update trip status. DEVELOPER is restricted from this action, and CUSTOMER_SERVICE, CLIENT, and others are not allowed.")
    })
    @PreAuthorize("(hasRole('PROVIDER') or hasRole('ADMIN')) and @tripService.isAuthorizedProviderTrip(#id, principal.id)")
    public ResponseEntity<TripDTO> updateTripStatus(
            @PathVariable Long id,
            @RequestBody TripStatusRequest statusRequest) {
        try {
            Trips trip = tripService.updateTripStatus(id, statusRequest.getStatus());
            return ResponseEntity.ok(mapToDTO(trip));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER/DRIVER or ADMIN can update trip status. DEVELOPER is restricted from this action, and CUSTOMER_SERVICE, CLIENT, and others are not allowed.");
        }
    }

    @PutMapping("/{id}/stops")
    @Operation(summary = "Add additional stops to a trip", description = "Adds additional stops to a specific trip")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Additional stops added successfully"),
            @ApiResponse(responseCode = "400", description = "Trip not in progress"),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER/DRIVER or ADMIN can add stops. DEVELOPER is restricted from this action, and CUSTOMER_SERVICE, CLIENT, and others are not allowed.")
    })
    @PreAuthorize("(hasRole('PROVIDER') or hasRole('ADMIN')) and @tripService.isAuthorizedProviderTrip(#id, principal.id)")
    public ResponseEntity<TripDTO> addStops(
            @PathVariable Long id,
            @RequestBody AdditionalStopsRequest stopsRequest) {
        try {
            Trips trip = tripService.addStops(id, stopsRequest.getStops());
            return ResponseEntity.ok(mapToDTO(trip));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER/DRIVER or ADMIN can add stops. DEVELOPER is restricted from this action, and CUSTOMER_SERVICE, CLIENT, and others are not allowed.");
        }
    }

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "Get all trips for a provider", description = "Fetches all trips for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trips retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #providerId), ADMIN, or DEVELOPER can access this endpoint. CUSTOMER_SERVICE, CLIENT, and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #providerId) or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<TripDTO>> getProviderTrips(@PathVariable Long providerId) {
        try {
            List<Trips> trips = tripService.getProviderTrips(providerId);
            List<TripDTO> tripDTOs = trips.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tripDTOs);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #providerId), ADMIN, or DEVELOPER can access this endpoint. CUSTOMER_SERVICE, CLIENT, and others are restricted.");
        }
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all trips for a client", description = "Fetches all trips for a specific client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trips retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT (if principal.id == #clientId), ADMIN, or DEVELOPER can access this endpoint. CUSTOMER_SERVICE, PROVIDER, and others are restricted.")
    })
    @PreAuthorize("(hasRole('CLIENT') and principal.id == #clientId) or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<TripDTO>> getClientTrips(@PathVariable Long clientId) {
        try {
            List<Trips> trips = tripService.getClientTrips(clientId);
            List<TripDTO> tripDTOs = trips.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tripDTOs);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT (if principal.id == #clientId), ADMIN, or DEVELOPER can access this endpoint. CUSTOMER_SERVICE, PROVIDER, and others are restricted.");
        }
    }

    // Helper method to map Trips entity to TripDTO
    private TripDTO mapToDTO(Trips trip) {
        return new TripDTO(
                trip.getId(),
                trip.getBookingId(),
                trip.getProviderId(),
                trip.getClientId(),
                trip.getDriverId(),
                trip.getVehicleId(),
                trip.getStatus(),
                trip.getStartTime(),
                trip.getEndTime(),
                trip.getAdditionalStops()
        );
    }
}