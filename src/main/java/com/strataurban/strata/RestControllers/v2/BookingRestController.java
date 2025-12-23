package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Security.LoggedUser;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.Services.v2.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/api/v2/bookings")
@Tag(name = "Booking Management", description = "APIs for managing booking requests")
public class BookingRestController {

    private final BookingService bookingService;

    @Autowired
    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }



    @PostMapping
    @Operation(summary = "Create a new booking request", description = "Allows a client to create a new booking request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = BookingRequestResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid booking request"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT can create a booking. ADMIN, PROVIDER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingRequestResponseDTO> createBooking(
            @Valid @RequestBody BookingRequestRequestDTO bookingRequestDTO, @LoggedUser SecurityUserDetails userDetails) {
        try {
            BookingRequest booking = bookingService.createBooking(bookingRequestDTO, userDetails.getId());
            BookingRequestResponseDTO responseDTO = bookingService.mapToResponseDTO(booking);
            return ResponseEntity.ok(responseDTO);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT can create a booking. ADMIN, PROVIDER, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all bookings for a client", description = "Fetches all bookings for a specific client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT (if principal.id == #clientId), ADMIN, or DEVELOPER can access this endpoint. PROVIDER and others are restricted.")
    })
    @PreAuthorize("hasRole('CLIENT')  or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<Page<BookingRequest>> getClientBookings(@PathVariable Long clientId, @LoggedUser SecurityUserDetails userDetails, Pageable pageable) {
        try {

            return ResponseEntity.ok(bookingService.getClientBookings(userDetails.getId(), pageable));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT (if principal.id == #clientId), ADMIN, or DEVELOPER can access this endpoint. PROVIDER and others are restricted.");
        }
    }

    @GetMapping("/provider")
    @Operation(summary = "Get all bookings for a provider", description = "Fetches all bookings for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #providerId), ADMIN, or DEVELOPER can access this endpoint. CLIENT and others are restricted.")
    })
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getProviderBookings(@LoggedUser SecurityUserDetails userDetails, Pageable pageable) {
        try {

            return ResponseEntity.ok(bookingService.getProviderBookings(userDetails.getId(), pageable));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #providerId), ADMIN, or DEVELOPER can access this endpoint. CLIENT and others are restricted.");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking by ID", description = "Retrieve details of a specific booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking found",
                    content = @Content(schema = @Schema(implementation = BookingRequestResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "hasRole('CUSTOMER_SERVICE') or " +
                    "hasRole('DEVELOPER') or " +
                    // PROVIDER can only see bookings they’re assigned to:
                    "(hasRole('PROVIDER')or " +
                    // CLIENT can only see their own bookings:
                    "(hasRole('CLIENT') and @methodSecurity.isClientOwner(#id, principal.id)))"
    )
    public ResponseEntity<BookingRequestResponseDTO> getBookingById(
            @Parameter(description = "Booking ID", example = "1") @PathVariable Long id,
            @LoggedUser SecurityUserDetails userDetails) {
        BookingRequest booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(bookingService.mapToResponseDTO(booking));
    }


    @PutMapping("/{id}/status")
    @Operation(summary = "Update booking status", description = "Update the status of a specific booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking status updated",
                    content = @Content(schema = @Schema(implementation = BookingRequestResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER or ADMIN can update booking status. CLIENT, DEVELOPER, and others are not allowed.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and @methodSecurity.isProviderOwner(#id, principal.id))  or hasRole('ADMIN')")
    public ResponseEntity<BookingRequestResponseDTO> updateBookingStatus(
            @Parameter(description = "Booking ID", example = "1") @PathVariable Long id,
            @Parameter(description = "New booking status", example = "ACCEPTED") @RequestBody(required = false) BookingStatus status) {
        try {
            if(ObjectUtils.isEmpty(status)){
                throw new RuntimeException("Please enter a status");
            }
            BookingRequest booking = bookingService.updateBookingStatus(id, status);
            BookingRequestResponseDTO responseDTO = bookingService.mapToResponseDTO(booking);
            return ResponseEntity.ok(responseDTO);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER or ADMIN can update booking status. CLIENT, DEVELOPER, and others are not allowed.");
        }
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a booking", description = "Allows a provider to confirm a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Booking cannot be confirmed"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER or ADMIN can confirm a booking. CLIENT, DEVELOPER, and others are not allowed.")
    })
    @PreAuthorize("(hasRole('PROVIDER')) or hasRole('ADMIN')")
    public ResponseEntity<BookingRequest> confirmBooking(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.confirmBooking(id));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER or ADMIN can confirm a booking. CLIENT, DEVELOPER, and others are not allowed.");
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking", description = "Allows a client or provider to cancel a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Booking cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT (if authorized) or PROVIDER (if authorized) can cancel a booking. ADMIN can also cancel, but DEVELOPER and others are restricted.")
    })
    @PreAuthorize("(hasRole('CLIENT') and @methodSecurity.isClientOwner(#id, principal.id)) or hasRole('CUSTOMER_SERVICE')")
    public ResponseEntity<BookingRequest> cancelBooking(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.cancelBooking(id));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT (if authorized) or PROVIDER (if authorized) can cancel a booking. ADMIN can also cancel, but DEVELOPER and others are restricted.");
        }
    }

    @PostMapping("/{id}/assign-driver")
    @Operation(summary = "Assign a driver to a booking", description = "Allows a provider to assign a driver to a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Driver cannot be assigned"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER or ADMIN can assign a driver. CLIENT, DEVELOPER, and others are not allowed.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and @methodSecurity.isProviderOwner(#id, principal.id)) or hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE')")
    public ResponseEntity<BookingRequest> assignDriver(
            @PathVariable Long id,
            @RequestBody DriverAssignmentRequest request) {
        try {
            return ResponseEntity.ok(bookingService.assignDriver(id, request));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER or ADMIN can assign a driver. CLIENT, DEVELOPER, and others are not allowed.");
        }
    }

    @GetMapping("/provider/{providerId}/status")
    @Operation(summary = "Get bookings by status for a provider", description = "Fetches bookings for a provider by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #providerId) or ADMIN can access this endpoint. CLIENT, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #providerId) or hasRole('CUSTOMER_SERVICE')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getProviderBookingsByStatus(
            @PathVariable Long providerId,
            @RequestParam BookingStatus status,
            Pageable pageable) {
        try {
            return ResponseEntity.ok(bookingService.getProviderBookingsByStatus(providerId, status, pageable));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #providerId) or ADMIN can access this endpoint. CLIENT, DEVELOPER, and others are restricted.");
        }
    }

    @PostMapping("/{id}/contact")
    @Operation(summary = "Contact a party", description = "Initiates contact with a party involved in a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact initiated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT (if authorized) or PROVIDER (if authorized) can contact a party. ADMIN can also contact, but DEVELOPER and others are restricted.")
    })
    @PreAuthorize("(hasRole('CLIENT') and @methodSecurity.isClientOwner(#id, principal.id)) or (hasRole('PROVIDER') and @methodSecurity.isProviderOwner(#id, principal.id)) or hasRole('ADMIN')")
    public ResponseEntity<Void> contactParty(
            @PathVariable Long id,
            @RequestBody ContactRequest request) {
        try {
            bookingService.contactParty(id, request);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT (if authorized) or PROVIDER (if authorized) can contact a party. ADMIN can also contact, but DEVELOPER and others are restricted.");
        }
    }

    @GetMapping("/client/{clientId}/history")
    @Operation(summary = "Get booking history for a client", description = "Fetches booking history for a specific client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT (if principal.id == #clientId), ADMIN, or DEVELOPER can access this endpoint. PROVIDER and others are restricted.")
    })
    @PreAuthorize("(hasRole('CLIENT') and principal.id == #clientId) or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getClientBookingHistory(@PathVariable Long clientId, Pageable pageable) {
        try {
            return ResponseEntity.ok(bookingService.getClientBookingHistory(clientId, pageable));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT (if principal.id == #clientId), ADMIN, or DEVELOPER can access this endpoint. PROVIDER and others are restricted.");
        }
    }

    @GetMapping("/provider/{providerId}/history")
    @Operation(summary = "Get booking history for a provider", description = "Fetches booking history for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #providerId) or ADMIN can access this endpoint. CLIENT, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #providerId) or hasRole('ADMIN')")
    public ResponseEntity<List<BookingRequest>> getProviderBookingHistory(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(bookingService.getProviderBookingHistory(providerId));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #providerId) or ADMIN can access this endpoint. CLIENT, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending bookings with filters", description = "Retrieve paginated pending bookings with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of pending bookings",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER or ADMIN can access this endpoint. CLIENT, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getPendingBookingsWithFilters(
            @Parameter(description = "Pick-up location", example = "Lagos", required = false) @RequestParam(required = false) String pickUpLocation,
            @Parameter(description = "Destination", example = "Abuja", required = false) @RequestParam(required = false) String destination,
            @Parameter(description = "Country", example = "Abuja", required = false) @RequestParam(required = false) String country,
            @Parameter(description = "City", example = "Abuja", required = false) @RequestParam(required = false) String city,
            @Parameter(description = "State", example = "Abuja", required = false) @RequestParam(required = false) String state,
            @Parameter(description = "Additional stops", example = "Ikeja", required = false) @RequestParam(required = false) String additionalStops,
            @Parameter(description = "Service start date", example = "2025-05-01T00:00:00", required = false) @RequestParam(required = false) LocalDateTime serviceStartDate,
            @Parameter(description = "Service end date", example = "2025-05-31T23:59:59", required = false) @RequestParam(required = false) LocalDateTime serviceEndDate,
            @Parameter(description = "Pick-up start date-time", example = "2025-05-01T00:00:00", required = false) @RequestParam(required = false) LocalDateTime pickupStartDateTime,
            @Parameter(description = "Pick-up end date-time", example = "2025-05-31T23:59:59", required = false) @RequestParam(required = false) LocalDateTime pickupEndDateTime,
            @Parameter(description = "Created start date", example = "2025-04-01T00:00:00", required = false) @RequestParam(required = false) LocalDateTime createdStartDate,
            @Parameter(description = "Created end date", example = "2025-04-30T23:59:59", required = false) @RequestParam(required = false) LocalDateTime createdEndDate,
            @Parameter(description = "Priority", example = "STANDARD", required = false) @RequestParam(required = false) EnumPriority priority,
            @Parameter(description = "Is passenger booking", example = "true", required = false) @RequestParam(required = false) Boolean isPassenger,
            @Parameter(description = "Number of passengers", example = "4", required = false) @RequestParam(required = false) Integer numberOfPassengers,
            @Parameter(description = "Event type", example = "Business", required = false) @RequestParam(required = false) String eventType,
            @Parameter(description = "Is cargo booking", example = "true", required = false) @RequestParam(required = false) Boolean isCargo,
            @Parameter(description = "Estimated weight in kg", example = "100.5", required = false) @RequestParam(required = false) Double estimatedWeightKg,
            @Parameter(description = "Supply type", example = "Electronics", required = false) @RequestParam(required = false) String supplyType,
            @Parameter(description = "Is medical booking", example = "true", required = false) @RequestParam(required = false) Boolean isMedical,
            @Parameter(description = "Medical item type", example = "Medical supplies", required = false) @RequestParam(required = false) String medicalItemType,
            @Parameter(description = "Is furniture booking", example = "true", required = false) @RequestParam(required = false) Boolean isFurniture,
            @Parameter(description = "Furniture type", example = "Sofa", required = false) @RequestParam(required = false) String furnitureType,
            @Parameter(description = "Is food booking", example = "true", required = false) @RequestParam(required = false) Boolean isFood,
            @Parameter(description = "Food type", example = "Perishable", required = false) @RequestParam(required = false) String foodType,
            @Parameter(description = "Is equipment booking", example = "true", required = false) @RequestParam(required = false) Boolean isEquipment,
            @Parameter(description = "Equipment item", example = "Projector", required = false) @RequestParam(required = false) String equipmentItem,
            @Parameter(description = "Pagination information") Pageable pageable) {
        try {
            Page<BookingRequest> bookings = bookingService.getPendingBookingsWithFilters(
                    pickUpLocation, destination, additionalStops,
                    serviceStartDate, serviceEndDate, pickupStartDateTime, pickupEndDateTime,
                    createdStartDate, createdEndDate, priority,
                    isPassenger, numberOfPassengers, eventType,
                    isCargo, estimatedWeightKg, supplyType,
                    isMedical, medicalItemType,
                    isFurniture, furnitureType,
                    isFood, foodType,
                    isEquipment, equipmentItem, city, state, country,
                    pageable);
            Page<BookingRequestResponseDTO> responseDTOs = bookings.map(bookingService::mapToResponseDTO);
            return ResponseEntity.ok(responseDTOs);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER or ADMIN can access this endpoint. CLIENT, DEVELOPER, and others are restricted.");
        }
    }


    @GetMapping("")
    @Operation(summary = "Get all bookings with filters", description = "Retrieve paginated pending bookings with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of pending bookings",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN and CUSTOMER SERVICE and DEVELOPERS can access this endpoint. CLIENT, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasAnyRole('CUSTOMER_SERVICE', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getAllBookings(
            @Parameter(description = "Booking Status", example = "PENDING", required = false) @RequestParam(required = false) BookingStatus status,
            @Parameter(description = "Pick-up location", example = "Lagos", required = false) @RequestParam(required = false) String pickUpLocation,
            @Parameter(description = "Destination", example = "Abuja", required = false) @RequestParam(required = false) String destination,
            @Parameter(description = "Country", example = "Abuja", required = false) @RequestParam(required = false) String country,
            @Parameter(description = "City", example = "Abuja", required = false) @RequestParam(required = false) String city,
            @Parameter(description = "State", example = "Abuja", required = false) @RequestParam(required = false) String state,
            @Parameter(description = "Additional stops", example = "Ikeja", required = false) @RequestParam(required = false) String additionalStops,
            @Parameter(description = "Service start date", example = "2025-05-01T00:00:00", required = false) @RequestParam(required = false) LocalDateTime serviceStartDate,
            @Parameter(description = "Service end date", example = "2025-05-31T23:59:59", required = false) @RequestParam(required = false) LocalDateTime serviceEndDate,
            @Parameter(description = "Pick-up start date-time", example = "2025-05-01T00:00:00", required = false) @RequestParam(required = false) LocalDateTime pickupStartDateTime,
            @Parameter(description = "Pick-up end date-time", example = "2025-05-31T23:59:59", required = false) @RequestParam(required = false) LocalDateTime pickupEndDateTime,
            @Parameter(description = "Created start date", example = "2025-04-01T00:00:00", required = false) @RequestParam(required = false) LocalDateTime createdStartDate,
            @Parameter(description = "Created end date", example = "2025-04-30T23:59:59", required = false) @RequestParam(required = false) LocalDateTime createdEndDate,
            @Parameter(description = "Priority", example = "STANDARD", required = false) @RequestParam(required = false) EnumPriority priority,
            @Parameter(description = "Is passenger booking", example = "true", required = false) @RequestParam(required = false) Boolean isPassenger,
            @Parameter(description = "Number of passengers", example = "4", required = false) @RequestParam(required = false) Integer numberOfPassengers,
            @Parameter(description = "Event type", example = "Business", required = false) @RequestParam(required = false) String eventType,
            @Parameter(description = "Is cargo booking", example = "true", required = false) @RequestParam(required = false) Boolean isCargo,
            @Parameter(description = "Estimated weight in kg", example = "100.5", required = false) @RequestParam(required = false) Double estimatedWeightKg,
            @Parameter(description = "Supply type", example = "Electronics", required = false) @RequestParam(required = false) String supplyType,
            @Parameter(description = "Is medical booking", example = "true", required = false) @RequestParam(required = false) Boolean isMedical,
            @Parameter(description = "Medical item type", example = "Medical supplies", required = false) @RequestParam(required = false) String medicalItemType,
            @Parameter(description = "Is furniture booking", example = "true", required = false) @RequestParam(required = false) Boolean isFurniture,
            @Parameter(description = "Furniture type", example = "Sofa", required = false) @RequestParam(required = false) String furnitureType,
            @Parameter(description = "Is food booking", example = "true", required = false) @RequestParam(required = false) Boolean isFood,
            @Parameter(description = "Food type", example = "Perishable", required = false) @RequestParam(required = false) String foodType,
            @Parameter(description = "Is equipment booking", example = "true", required = false) @RequestParam(required = false) Boolean isEquipment,
            @Parameter(description = "Equipment item", example = "Projector", required = false) @RequestParam(required = false) String equipmentItem,
            @Parameter(description = "Pagination information") Pageable pageable) {
        try {
            Page<BookingRequest> bookings = bookingService.getAllBookings(status,
                    pickUpLocation, destination, additionalStops,
                    serviceStartDate, serviceEndDate, pickupStartDateTime, pickupEndDateTime,
                    createdStartDate, createdEndDate, priority,
                    isPassenger, numberOfPassengers, eventType,
                    isCargo, estimatedWeightKg, supplyType,
                    isMedical, medicalItemType,
                    isFurniture, furnitureType,
                    isFood, foodType,
                    isEquipment, equipmentItem, city, state, country,
                    pageable);
            Page<BookingRequestResponseDTO> responseDTOs = bookings.map(bookingService::mapToResponseDTO);
            return ResponseEntity.ok(responseDTOs);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER or ADMIN can access this endpoint. CLIENT, DEVELOPER, and others are restricted.");
        }
    }
}