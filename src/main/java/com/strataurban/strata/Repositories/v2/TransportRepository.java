package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
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
    @Query("SELECT t FROM Transport t WHERE t.providerId = :providerId " +
            "AND t.status = 'Available' " +
            "AND (:transportCategory IS NULL OR t.type = :transportCategory) " +
            "AND (:capacity IS NULL OR t.capacity >= :capacity)")
    List<Transport> findAvailableTransports(Long providerId, String transportCategory, Integer capacity);

    // Find all transports for a specific provider
    Integer countByProviderId(Long providerId);
}