package com.strataurban.strata.Services.v2;

import com.strataurban.strata.DTOs.v2.TripTracking;
import com.strataurban.strata.Entities.RequestEntities.Trips;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.TripStatus;

import java.util.List;

public interface TripService {

    // Start a trip
    Trips startTrip(Long bookingId);

    // End a trip
    Trips endTrip(Long id);

    // Get trip details by ID
    Trips getTripById(Long id);

    // Track a trip
    TripTracking trackTrip(Long id);

    // Update trip status
    Trips updateTripStatus(Long id, BookingStatus status);

    // Add additional stops to a trip
    Trips addStops(Long id, List<String> stops);

    // Get all trips for a provider
    List<Trips> getProviderTrips(Long providerId);

    // Get all trips for a client
    List<Trips> getClientTrips(Long clientId);
}