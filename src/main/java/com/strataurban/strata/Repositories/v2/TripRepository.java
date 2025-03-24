package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.RequestEntities.Trips;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trips, Long> {

    // Find all trips for a specific provider
    List<Trips> findByProviderId(Long providerId);

    // Find all trips for a specific client
    List<Trips> findByClientId(Long clientId);

    // Find a trip by booking ID
    Optional<Trips> findByBookingId(Long bookingId);
}