package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.AcceptOfferRequest;
import com.strataurban.strata.DTOs.v2.ContactRequest;
import com.strataurban.strata.DTOs.v2.CreateOfferRequest;
import com.strataurban.strata.DTOs.v2.DriverAssignmentRequest;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Services.v2.BookingService;
import com.strataurban.strata.Services.v2.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            @ApiResponse(responseCode = "200", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid booking request")
    })
    public ResponseEntity<BookingRequest> createBooking(
            @RequestBody BookingRequest bookingRequest,
            @RequestParam Long clientId) {
        return ResponseEntity.ok(bookingService.createBooking(bookingRequest, clientId));
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
    @Operation(summary = "Get booking details by ID", description = "Fetches details of a specific booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingRequest> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update booking status", description = "Updates the status of a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingRequest> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
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
    @Operation(summary = "Get all pending bookings with filters", description = "Fetches all pending bookings with optional filters, paginated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending bookings retrieved successfully")
    })
    public ResponseEntity<Page<BookingRequest>> getPendingBookings(
            @RequestParam(required = false) String pickupCountry,
            @RequestParam(required = false) String pickupState,
            @RequestParam(required = false) String pickupCity,
            @RequestParam(required = false) String pickupStreet,
            @RequestParam(required = false) String pickupLga,
            @RequestParam(required = false) String pickupTown,
            @RequestParam(required = false) String destinationCountry,
            @RequestParam(required = false) String destinationState,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) String destinationStreet,
            @RequestParam(required = false) String destinationLga,
            @RequestParam(required = false) String destinationTown,
            @RequestParam(required = false) LocalDateTime serviceStartDate,
            @RequestParam(required = false) LocalDateTime serviceEndDate,
            @RequestParam(required = false) LocalDateTime createdStartDate,
            @RequestParam(required = false) LocalDateTime createdEndDate,
            @RequestParam(required = false) EnumPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getPendingBookingsWithFilters(
                pickupCountry, pickupState, pickupCity, pickupStreet, pickupLga, pickupTown,
                destinationCountry, destinationState, destinationCity, destinationStreet, destinationLga, destinationTown,
                serviceStartDate, serviceEndDate, createdStartDate, createdEndDate, priority, pageable));
    }
    @GetMapping
    @Operation(summary = "Get all bookings", description = "Fetches all booking requests in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
    })
    public ResponseEntity<List<BookingRequest>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
}