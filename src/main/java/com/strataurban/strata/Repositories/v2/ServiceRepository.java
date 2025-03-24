package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Services, Long> {

    // Find all services for a specific provider
    List<Services> findByProviderId(Long providerId);
}