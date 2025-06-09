package com.strataurban.strata.Services.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    // Create a new booking request
    BookingRequest createBooking(BookingRequestRequestDTO bookingRequest, Long clientId);

    // Get all bookings for a client
    Page<BookingRequest> getClientBookings(Long clientId, Pageable pageable);

    // Get all bookings for a provider
    Page<BookingRequestResponseDTO> getProviderBookings(Long providerId, Pageable pageable);

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
    Page<BookingRequestResponseDTO> getProviderBookingsByStatus(Long providerId, BookingStatus status, Pageable pageable);

    // Contact a party involved in a booking
    void contactParty(Long id, ContactRequest request);

    // Get booking history for a client
    Page<BookingRequestResponseDTO> getClientBookingHistory(Long clientId, Pageable pageable);

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
            Boolean isFood, String foodType, Boolean isEquipment, String equipmentItem, String city, String state, String country,
            Pageable pageable);

    Page<BookingRequest> getAllBookings(BookingStatus status,
            String pickUpLocation, String destination, String additionalStops,
            LocalDateTime serviceStartDate, LocalDateTime serviceEndDate,
            LocalDateTime pickupStartDateTime, LocalDateTime pickupEndDateTime,
            LocalDateTime createdStartDate, LocalDateTime createdEndDate,
            EnumPriority priority, Boolean isPassenger, Integer numberOfPassengers,
            String eventType, Boolean isCargo, Double estimatedWeightKg, String supplyType,
            Boolean isMedical, String medicalItemType, Boolean isFurniture, String furnitureType,
            Boolean isFood, String foodType, Boolean isEquipment, String equipmentItem, String city, String state, String country,
            Pageable pageable);

    BookingRequest acceptOffer(Long bookingId, Long offerId);

    List<BookingRequest> getAllBookings();

    BookingRequestResponseDTO mapToResponseDTO(BookingRequest entity);

    List<DriverResponse> getAvailableDrivers();
}