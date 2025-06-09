package com.strataurban.strata.Services.v2;

import com.strataurban.strata.DTOs.v2.ProviderDashboard;
import com.strataurban.strata.DTOs.v2.RatingRequest;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ProviderDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProviderService {

    // Register a new provider
    Provider registerProvider(Provider provider);

    // Get provider profile by ID
    Provider getProviderById(Long id);

    // Update provider profile
    Provider updateProvider(Long id, Provider provider);

    // Upload provider documents
    ProviderDocument uploadDocuments(Long providerId, ProviderDocument documents);

    // Get provider documents
    ProviderDocument getProviderDocuments(Long providerId);

    // Update provider documents
    ProviderDocument updateProviderDocuments(Long providerId, ProviderDocument documents);

    // Delete provider account
    void deleteProvider(Long id);

    Page<Provider> getAllProviders(Pageable pageable);
    Page<Provider> searchProviders(String name, String serviceType, String city, Double minRating, Pageable pageable);
    // Rate a provider
    void rateProvider(Long providerId, RatingRequest rating);

    // Get provider dashboard statistics
    ProviderDashboard getProviderDashboard(Long providerId);
}