package com.strataurban.strata.Services.v2;

import com.strataurban.strata.DTOs.v2.BookingRequestRequestDTO;
import com.strataurban.strata.DTOs.v2.BookingRequestResponseDTO;
import com.strataurban.strata.DTOs.v2.ContactRequest;
import com.strataurban.strata.DTOs.v2.DriverAssignmentRequest;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    // Create a new booking request
    BookingRequest createBooking(BookingRequestRequestDTO bookingRequest, Long clientId);

    // Get all bookings for a client
    List<BookingRequest> getClientBookings(Long clientId);

    // Get all bookings for a provider
    List<BookingRequest> getProviderBookings(Long providerId);

    // Get booking details by ID
    BookingRequest getBookingById(Long id);

    // Update booking status
    BookingRequest updateBookingStatus(Long id, BookingStatus status);

    // Confirm a booking
    BookingRequest confirmBooking(Long id);

    // Cancel a booking
    BookingRequest cancelBooking(Long id);

    // Assign a driver to a booking
    BookingRequest assignDriver(Long id, DriverAssignmentRequest request);

    // Get bookings by status for a provider
    List<BookingRequest> getProviderBookingsByStatus(Long providerId, BookingStatus status);

    // Contact a party involved in a booking
    void contactParty(Long id, ContactRequest request);

    // Get booking history for a client
    List<BookingRequest> getClientBookingHistory(Long clientId);

    // Get booking history for a provider
    List<BookingRequest> getProviderBookingHistory(Long providerId);


    // Confirm a booking
    BookingRequest claimBooking(Long id, Long providerId);

    Page<BookingRequest> getPendingBookingsWithFilters(
            String pickUpLocation, String destination, String additionalStops,
            LocalDateTime serviceStartDate, LocalDateTime serviceEndDate,
            LocalDateTime pickupStartDateTime, LocalDateTime pickupEndDateTime,
            LocalDateTime createdStartDate, LocalDateTime createdEndDate,
            EnumPriority priority, Boolean isPassenger, Integer numberOfPassengers,
            String eventType, Boolean isCargo, Double estimatedWeightKg, String supplyType,
            Boolean isMedical, String medicalItemType, Boolean isFurniture, String furnitureType,
            Boolean isFood, String foodType, Boolean isEquipment, String equipmentItem,
            Pageable pageable);

    BookingRequest acceptOffer(Long bookingId, Long offerId);

    List<BookingRequest> getAllBookings();

    BookingRequestResponseDTO mapToResponseDTO(BookingRequest entity);

    Long getClientIdByUsername(String username);
}