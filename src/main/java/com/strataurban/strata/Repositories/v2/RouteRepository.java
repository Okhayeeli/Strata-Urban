package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Routes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Routes, Long>, JpaSpecificationExecutor<Routes> {


    List<Routes> findAllByCountryAndIsEnabledTrue(String country);
    List<Routes> findAllByCountryAndStateAndIsEnabledTrue(String country, String state);
    List<Routes> findAllByCountryAndStateAndCityAndIsEnabledTrue(String country, String state, String city);
    List<Routes> findByStartContainingAndEndContainingAndIsEnabledTrue(String startLocation, String endLocation);
    List<Routes> findAllByProviderIdAndIsEnabledTrue(String supplierId);
    Boolean existsByStartAndEndAndCountryAndIsEnabled(String start, String end, String country, Boolean isEnabled);
    /**
     * Find all routes where a provider is involved
     * Searches for the provider ID in the comma-separated providerId field

     * Pattern matching explanation:
     * - CONCAT(',', provider_id, ',') creates: ",123,456,789,"
     * - CONCAT('%,', :providerId, ',%') creates: "%,456,%"
     * - This ensures we match the exact provider ID, not partial matches
     *   (e.g., searching for "5" won't match "456")
     */
    @Query("SELECT r FROM Routes r WHERE " +
            "CONCAT(',', r.providerId, ',') LIKE CONCAT('%,', :providerId, ',%') " +
            "AND r.isEnabled = true")
    List<Routes> findRoutesByProviderId(@Param("providerId") String providerId);

    /**
     * Get just the route IDs where a provider is involved
     * More efficient - returns only IDs instead of full Route objects
     */
    @Query("SELECT r.id FROM Routes r WHERE " +
            "CONCAT(',', r.providerId, ',') LIKE CONCAT('%,', :providerId, ',%') " +
            "AND r.isEnabled = true")
    List<Long> findRouteIdsByProviderId(@Param("providerId") String providerId);

    /**
     * Find routes by multiple criteria including provider involvement
     */
    @Query("SELECT r FROM Routes r WHERE " +
            "(CONCAT(',', r.providerId, ',') LIKE CONCAT('%,', :providerId, ',%') OR :providerId IS NULL) " +
            "AND (:country IS NULL OR r.country = :country) " +
            "AND (:state IS NULL OR r.state = :state) " +
            "AND (:city IS NULL OR r.city = :city) " +
            "AND r.isEnabled = true")
    List<Routes> findRoutesByProviderIdAndLocation(
            @Param("providerId") String providerId,
            @Param("country") String country,
            @Param("state") String state,
            @Param("city") String city
    );


    // Count routes by various criteria
    @Query("SELECT COUNT(r) FROM Routes r WHERE r.isEnabled = true")
    Long countEnabledRoutes();

    @Query("SELECT COUNT(DISTINCT r.country) FROM Routes r WHERE r.isEnabled = true")
    Long countActiveCountries();

    // Search routes by start or end location
    @Query("SELECT r FROM Routes r WHERE " +
            "(LOWER(r.start) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(r.end) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND r.isEnabled = true")
    List<Routes> searchRoutesByLocation(@Param("search") String search);
}
