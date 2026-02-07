package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.Passengers.Driver;
import com.strataurban.strata.Enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long>, JpaSpecificationExecutor<Driver> {

    // Find by providerId
    List<Driver> findByProviderId(String providerId);

    // Count by providerId
    Integer countByProviderId(String providerId);

    // Find by email
    Optional<Driver> findByEmail(String email);

    // Find by username
    Optional<Driver> findByUsername(String username);

    // Find by driver status
    List<Driver> findByDriverStatus(DriverStatus driverStatus);

    // Find active drivers by provider
    List<Driver> findByProviderIdAndIsActiveTrue(String providerId);

    // Find available drivers (ready for trips)
    @Query("SELECT d FROM Driver d WHERE d.isActive = true AND d.isAvailable = true AND d.isOnTrip = false " +
           "AND d.emailVerified = true AND d.driverStatus = 'AVAILABLE'")
    List<Driver> findAvailableDrivers();

    // Find available drivers by provider
    @Query("SELECT d FROM Driver d WHERE d.providerId = :providerId AND d.isActive = true AND d.isAvailable = true " +
           "AND d.emailVerified = true AND d.driverStatus = 'AVAILABLE'")
    List<Driver> findAvailableDriversByProvider(@Param("providerId") String providerId);

    // Find drivers on trip
    List<Driver> findByIsOnTripTrue();


    // Find top rated drivers
    @Query("SELECT d FROM Driver d WHERE d.rating IS NOT NULL AND d.numberOfRatings > 0 " +
           "ORDER BY d.rating DESC, d.numberOfRatings DESC")
    List<Driver> findTopRatedDrivers();

    // Find drivers by rating range
    @Query("SELECT d FROM Driver d WHERE d.rating >= :minRating AND d.rating <= :maxRating")
    List<Driver> findByRatingBetween(@Param("minRating") Double minRating, @Param("maxRating") Double maxRating);

    // Find drivers by city
    List<Driver> findByCity(String city);

    // Find drivers by city and state
    List<Driver> findByCityAndState(String city, String state);

    // Find drivers last active after date
    List<Driver> findByLastActiveDateAfter(LocalDateTime date);

    // Find inactive drivers (not active in X days)
    @Query("SELECT d FROM Driver d WHERE d.lastActiveDate < :cutoffDate OR d.lastActiveDate IS NULL")
    List<Driver> findInactiveDriversSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Count drivers by provider and status
    @Query("SELECT COUNT(d) FROM Driver d WHERE d.providerId = :providerId AND d.driverStatus = :status")
    Long countByProviderIdAndStatus(@Param("providerId") String providerId, @Param("status") DriverStatus status);

    // Get total completed trips for provider
    @Query("SELECT SUM(d.completedTrips) FROM Driver d WHERE d.providerId = :providerId")
    Long getTotalCompletedTripsByProvider(@Param("providerId") String providerId);

    // Get average rating for provider's drivers
    @Query("SELECT AVG(d.rating) FROM Driver d WHERE d.providerId = :providerId AND d.numberOfRatings > 0")
    Double getAverageRatingByProvider(@Param("providerId") String providerId);

    // Search drivers by name
    @Query("SELECT d FROM Driver d WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(d.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Driver> searchDrivers(@Param("query") String query);


    // Count active drivers
    Long countByIsActiveTrue();

    // Count available drivers
    Long countByIsAvailableTrue();

    // Count drivers on trip
    Long countByIsOnTripTrue();

    // Exists by email
    boolean existsByEmail(String email);

    // Exists by username
    boolean existsByUsername(String username);
}
