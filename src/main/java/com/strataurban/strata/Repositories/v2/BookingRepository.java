package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingRequest, Long> {

    // Find all bookings for a specific client (based on the user who created the booking)
    List<BookingRequest> findByClientId(Long clientId);

    // Find all bookings for a specific provider
    List<BookingRequest> findByProviderId(Long providerId);

    // Find bookings by provider and status
    List<BookingRequest> findByProviderIdAndStatus(Long providerId, BookingStatus status);

    // Find booking history for a client (e.g., completed or cancelled bookings)
    List<BookingRequest> findByClientIdAndStatusIn(Long clientId, List<BookingStatus> statuses);

    // Find booking history for a provider (e.g., completed or cancelled bookings)
    List<BookingRequest> findByProviderIdAndStatusIn(Long providerId, List<BookingStatus> statuses);
}