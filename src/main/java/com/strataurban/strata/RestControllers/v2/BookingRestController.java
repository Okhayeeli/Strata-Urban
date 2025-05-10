package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.BookingRequestRequestDTO;
import com.strataurban.strata.DTOs.v2.BookingRequestResponseDTO;
import com.strataurban.strata.DTOs.v2.ContactRequest;
import com.strataurban.strata.DTOs.v2.DriverAssignmentRequest;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Services.v2.BookingService;
import com.strataurban.strata.Services.v2.OfferService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/v2/bookings")
@Tag(name = "Booking Management", description = "APIs for managing booking requests")
public class BookingRestController {

    private final BookingService bookingService;
    private final OfferService offerService;

    @Autowired
    public BookingRestController(BookingService bookingService, OfferService offerService) {
        this.bookingService = bookingService;
        this.offerService = offerService;
    }

    @PostMapping
    @Operation(summary = "Create a new booking request", description = "Allows a client to create a new booking request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = BookingRequestResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid booking request")
    })
    public ResponseEntity<BookingRequestResponseDTO> createBooking(
            @Valid @RequestBody BookingRequestRequestDTO bookingRequestDTO,
            @Parameter(description = "Client ID", example = "1") @RequestParam Long clientId) {
        BookingRequest booking = bookingService.createBooking(bookingRequestDTO, clientId);
        BookingRequestResponseDTO responseDTO = bookingService.mapToResponseDTO(booking);
        return ResponseEntity.ok(responseDTO);
    }



    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all bookings for a client", description = "Fetches all bookings for a specific client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<List<BookingRequest>> getClientBookings(@PathVariable Long clientId) {
        return ResponseEntity.ok(bookingService.getClientBookings(clientId));
    }

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "Get all bookings for a provider", description = "Fetches all bookings for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<List<BookingRequest>> getProviderBookings(@PathVariable Long providerId) {
        return ResponseEntity.ok(bookingService.getProviderBookings(providerId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking by ID", description = "Retrieve details of a specific booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking found",
                    content = @Content(schema = @Schema(implementation = BookingRequestResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingRequestResponseDTO> getBookingById(
            @Parameter(description = "Booking ID", example = "1") @PathVariable Long id) {
        BookingRequest booking = bookingService.getBookingById(id);
        BookingRequestResponseDTO responseDTO = bookingService.mapToResponseDTO(booking);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update booking status", description = "Update the status of a specific booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking status updated",
                    content = @Content(schema = @Schema(implementation = BookingRequestResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingRequestResponseDTO> updateBookingStatus(
            @Parameter(description = "Booking ID", example = "1") @PathVariable Long id,
            @Parameter(description = "New booking status", example = "ACCEPTED") @RequestBody BookingStatus status) {
        BookingRequest booking = bookingService.updateBookingStatus(id, status);
        BookingRequestResponseDTO responseDTO = bookingService.mapToResponseDTO(booking);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a booking", description = "Allows a provider to confirm a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Booking cannot be confirmed"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingRequest> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking", description = "Allows a client or provider to cancel a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Booking cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingRequest> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PostMapping("/{id}/assign-driver")
    @Operation(summary = "Assign a driver to a booking", description = "Allows a provider to assign a driver to a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Driver cannot be assigned"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingRequest> assignDriver(
            @PathVariable Long id,
            @RequestBody DriverAssignmentRequest request) {
        return ResponseEntity.ok(bookingService.assignDriver(id, request));
    }

    @GetMapping("/provider/{providerId}/status")
    @Operation(summary = "Get bookings by status for a provider", description = "Fetches bookings for a provider by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<List<BookingRequest>> getProviderBookingsByStatus(
            @PathVariable Long providerId,
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.getProviderBookingsByStatus(providerId, status));
    }

    @PostMapping("/{id}/contact")
    @Operation(summary = "Contact a party", description = "Initiates contact with a party involved in a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact initiated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Void> contactParty(
            @PathVariable Long id,
            @RequestBody ContactRequest request) {
        bookingService.contactParty(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/client/{clientId}/history")
    @Operation(summary = "Get booking history for a client", description = "Fetches booking history for a specific client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<List<BookingRequest>> getClientBookingHistory(@PathVariable Long clientId) {
        return ResponseEntity.ok(bookingService.getClientBookingHistory(clientId));
    }

    @GetMapping("/provider/{providerId}/history")
    @Operation(summary = "Get booking history for a provider", description = "Fetches booking history for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<List<BookingRequest>> getProviderBookingHistory(@PathVariable Long providerId) {
        return ResponseEntity.ok(bookingService.getProviderBookingHistory(providerId));
    }


    @GetMapping("/pending")
    @Operation(summary = "Get pending bookings with filters", description = "Retrieve paginated pending bookings with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of pending bookings",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<BookingRequestResponseDTO>> getPendingBookingsWithFilters(
            @Parameter(description = "Pick-up location", example = "Lagos", required = false) @RequestParam(required = false) String pickUpLocation,
            @Parameter(description = "Destination", example = "Abuja", required = false) @RequestParam(required = false) String destination,
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
        Page<BookingRequest> bookings = bookingService.getPendingBookingsWithFilters(
                pickUpLocation, destination, additionalStops,
                serviceStartDate, serviceEndDate, pickupStartDateTime, pickupEndDateTime,
                createdStartDate, createdEndDate, priority,
                isPassenger, numberOfPassengers, eventType,
                isCargo, estimatedWeightKg, supplyType,
                isMedical, medicalItemType,
                isFurniture, furnitureType,
                isFood, foodType,
                isEquipment, equipmentItem,
                pageable);
        Page<BookingRequestResponseDTO> responseDTOs = bookings.map(bookingService::mapToResponseDTO);
        return ResponseEntity.ok(responseDTOs);
    }
    @GetMapping
    @Operation(summary = "Get all bookings", description = "Retrieve a list of all bookings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of bookings",
                    content = @Content(schema = @Schema(implementation = BookingRequestResponseDTO.class)))
    })
    public ResponseEntity<List<BookingRequestResponseDTO>> getAllBookings() {
        List<BookingRequest> bookings = bookingService.getAllBookings();
        List<BookingRequestResponseDTO> responseDTOs = bookings.stream()
                .map(bookingService::mapToResponseDTO)
                .toList();
        return ResponseEntity.ok(responseDTOs);
    }
}