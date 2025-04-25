package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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



    @Query("SELECT br FROM BookingRequest br WHERE br.status = :status "
            + "AND (:pickupCountry IS NULL OR br.pickupCountry = :pickupCountry) "
            + "AND (:pickupState IS NULL OR br.pickupState = :pickupState) "
            + "AND (:pickupCity IS NULL OR br.pickupCity = :pickupCity) "
            + "AND (:pickupStreet IS NULL OR br.pickupStreet LIKE %:pickupStreet%) "
            + "AND (:pickupLga IS NULL OR br.pickupLga = :pickupLga) "
            + "AND (:pickupTown IS NULL OR br.pickupTown = :pickupTown) "
            + "AND (:destinationCountry IS NULL OR br.destinationCountry = :destinationCountry) "
            + "AND (:destinationState IS NULL OR br.destinationState = :destinationState) "
            + "AND (:destinationCity IS NULL OR br.destinationCity = :destinationCity) "
            + "AND (:destinationStreet IS NULL OR br.destinationStreet LIKE %:destinationStreet%) "
            + "AND (:destinationLga IS NULL OR br.destinationLga = :destinationLga) "
            + "AND (:destinationTown IS NULL OR br.destinationTown = :destinationTown) "
            + "AND (:serviceStartDate IS NULL OR br.serviceDate >= :serviceStartDate) "
            + "AND (:serviceEndDate IS NULL OR br.serviceDate <= :serviceEndDate) "
            + "AND (:createdStartDate IS NULL OR br.createdDate >= :createdStartDate) "
            + "AND (:createdEndDate IS NULL OR br.createdDate <= :createdEndDate) "
            + "AND (:priority IS NULL OR br.priority = :priority)")
    Page<BookingRequest> findByStatusAndFilters(
            @Param("status") BookingStatus status,
            @Param("pickupCountry") String pickupCountry,
            @Param("pickupState") String pickupState,
            @Param("pickupCity") String pickupCity,
            @Param("pickupStreet") String pickupStreet,
            @Param("pickupLga") String pickupLga,
            @Param("pickupTown") String pickupTown,
            @Param("destinationCountry") String destinationCountry,
            @Param("destinationState") String destinationState,
            @Param("destinationCity") String destinationCity,
            @Param("destinationStreet") String destinationStreet,
            @Param("destinationLga") String destinationLga,
            @Param("destinationTown") String destinationTown,
            @Param("serviceStartDate") LocalDateTime serviceStartDate,
            @Param("serviceEndDate") LocalDateTime serviceEndDate,
            @Param("createdStartDate") LocalDateTime createdStartDate,
            @Param("createdEndDate") LocalDateTime createdEndDate,
            @Param("priority") EnumPriority priority,
            Pageable pageable
    );
}