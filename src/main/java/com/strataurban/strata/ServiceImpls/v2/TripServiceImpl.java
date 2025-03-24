package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.DTOs.v2.TripTracking;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Entities.RequestEntities.Trips;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.TripStatus;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.TripRepository;
import com.strataurban.strata.Services.v2.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public TripServiceImpl(TripRepository tripRepository, BookingRepository bookingRepository) {
        this.tripRepository = tripRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Trips startTrip(Long bookingId) {
        // Check if a trip already exists for this booking
        Trips existingTrip = tripRepository.findByBookingId(bookingId).orElse(null);
        if (existingTrip != null) {
            throw new RuntimeException("A trip for booking ID " + bookingId + " has already been started");
        }

        // Fetch the booking
        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        // Validate booking status
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Only CONFIRMED bookings can start a trip");
        }

        // Create a new trip
        Trips trip = new Trips();
        trip.setBookingId(bookingId);
        trip.setProviderId(booking.getProviderId());
        trip.setClientId(booking.getClientId());
        trip.setDriverId(booking.getDriverId());
        trip.setVehicleId(booking.getVehicleId());
        trip.setStatus(BookingStatus.IN_PROGRESS);
        trip.setStartTime(LocalDateTime.now());

        // Update booking status
        booking.setStatus(BookingStatus.IN_PROGRESS);
        bookingRepository.save(booking);

        return tripRepository.save(trip);
    }

    @Override
    public Trips endTrip(Long id) {
        Trips trip = getTripById(id);
        if (trip.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new RuntimeException("Only IN_PROGRESS trips can be ended");
        }
        trip.setStatus(BookingStatus.COMPLETED);
        trip.setEndTime(LocalDateTime.now());

        // Update the associated booking status
        BookingRequest booking = bookingRepository.findById(trip.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + trip.getBookingId()));
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        return tripRepository.save(trip);
    }

    @Override
    public Trips getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + id));
    }

    @Override
    public TripTracking trackTrip(Long id) {
        Trips trip = getTripById(id);
        if (trip.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new RuntimeException("Only IN_PROGRESS trips can be tracked");
        }

        // In a real implementation, you'd integrate with a real-time location service (e.g., Google Maps API)
        // For now, we'll return mock data
        return new TripTracking(
                40.7128, // Mock latitude (e.g., New York)
                -74.0060, // Mock longitude
                15, // Mock ETA in minutes
                trip.getStatus()
        );
    }

    @Override
    public Trips updateTripStatus(Long id, BookingStatus status) {
        Trips trip = getTripById(id);
        // Validate status transition (e.g., can't go from COMPLETED to IN_PROGRESS)
        if (trip.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot update status of a COMPLETED trip");
        }
        trip.setStatus(status);

        // Update the associated booking status
        BookingRequest booking = bookingRepository.findById(trip.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + trip.getBookingId()));
        booking.setStatus(status);
        bookingRepository.save(booking);

        return tripRepository.save(trip);
    }

    @Override
    public Trips addStops(Long id, List<String> stops) {
        Trips trip = getTripById(id);
        if (trip.getStatus() != BookingStatus.IN_PROGRESS) { // Note: BookingStatus was incorrectly used here; it should be TripStatus
            throw new RuntimeException("Additional stops can only be added to IN_PROGRESS trips");
        }
        trip.appendStops(stops); // Use the helper method to append stops as a comma-separated string
        return tripRepository.save(trip);
    }

    @Override
    public List<Trips> getProviderTrips(Long providerId) {
        return tripRepository.findByProviderId(providerId);
    }

    @Override
    public List<Trips> getClientTrips(Long clientId) {
        return tripRepository.findByClientId(clientId);
    }
}