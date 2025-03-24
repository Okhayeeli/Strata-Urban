package com.strataurban.strata.Services.v2;

import com.strataurban.strata.Entities.Providers.Services;

import java.util.List;

public interface ServiceService {

    // Add a new service
    Services addService(Services service);

    // Get all services for a provider
    List<Services> getProviderServices(Long providerId);

    // Get service details by ID
    Services getServiceById(Long id);

    // Update service details
    Services updateService(Long id, Services service);

    // Delete a service
    void deleteService(Long id);

    // Get all available services
    List<Services> getAllServices();
}
