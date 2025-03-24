package com.strataurban.strata.Services.v2;

import com.strataurban.strata.Entities.Providers.Transport;

import java.util.List;

public interface TransportService {

    // Add a new transport
    Transport addTransport(Transport transport);

    // Get all transports for a provider
    List<Transport> getProviderTransports(Long providerId);

    // Get transport details by ID
    Transport getTransportById(Long id);

    // Update transport details
    Transport updateTransport(Long id, Transport transport);

    // Delete a transport
    void deleteTransport(Long id);

    // Get available transports for a booking
    List<Transport> getAvailableTransports(Long providerId, String transportCategory, Integer capacity);

    // Update transport status
    Transport updateTransportStatus(Long id, String status);
}
