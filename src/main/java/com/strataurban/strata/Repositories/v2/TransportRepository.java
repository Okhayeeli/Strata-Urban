package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Transport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {

    // Find all transports for a specific provider
    List<Transport> findByProviderId(Long providerId);

    // Find available transports for a provider based on category and capacity
    @Query("SELECT t FROM Transport t WHERE (:providerId IS NULL OR t.providerId = :providerId) " +
            "AND t.status = 'Available' " +
            "AND (:transportCategory IS NULL OR t.type = :transportCategory) " +
            "AND (:capacity IS NULL OR t.capacity >= :capacity)")
    List<Transport> findAvailableTransports(Long providerId, String transportCategory, Integer capacity);

    // Find all transports for a specific provider
    Integer countByProviderId(Long providerId);

    @Query("SELECT t FROM Transport t WHERE "
            + "(:providerId IS NULL OR t.providerId = :providerId) "
            + "AND (:type IS NULL OR t.type = :type) "
            + "AND (:capacity IS NULL OR t.capacity = :capacity) "
            + "AND (:description IS NULL OR t.description LIKE CONCAT('%', :description, '%')) "
            + "AND (:plateNumber IS NULL OR t.plateNumber LIKE CONCAT('%', :plateNumber, '%')) "
            + "AND (:brand IS NULL OR t.brand LIKE CONCAT('%', :brand, '%')) "
            + "AND (:model IS NULL OR t.model LIKE CONCAT('%', :model, '%')) "
            + "AND (:color IS NULL OR t.color LIKE CONCAT('%', :color, '%')) "
            + "AND (:state IS NULL OR t.state LIKE CONCAT('%', :state, '%')) "
            + "AND (:company IS NULL OR t.company LIKE CONCAT('%', :company, '%')) "
            + "AND (:routeId IS NULL OR t.routeId = :routeId) "
            + "AND (:status IS NULL OR t.status = :status)")
    Page<Transport> findByFilters(
            @Param("providerId") Long providerId,
            @Param("type") String type,
            @Param("capacity") Integer capacity,
            @Param("description") String description,
            @Param("plateNumber") String plateNumber,
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("color") String color,
            @Param("state") String state,
            @Param("company") String company,
            @Param("routeId") Long routeId,
            @Param("status") String status,
            Pageable pageable);
}