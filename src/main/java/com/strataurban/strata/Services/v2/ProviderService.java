package com.strataurban.strata.Services.v2;

import com.strataurban.strata.DTOs.v2.ProviderDashboard;
import com.strataurban.strata.DTOs.v2.RatingRequest;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ProviderDocument;

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

    // Get all providers
    List<Provider> getAllProviders();

    // Search providers by criteria
    List<Provider> searchProviders(String serviceType, String city, Double minRating);

    // Rate a provider
    void rateProvider(Long providerId, RatingRequest rating);

    // Get provider dashboard statistics
    ProviderDashboard getProviderDashboard(Long providerId);
}