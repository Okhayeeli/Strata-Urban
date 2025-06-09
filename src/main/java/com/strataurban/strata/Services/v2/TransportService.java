package com.strataurban.strata.Services.v2;

import com.strataurban.strata.Entities.Providers.Transport;
import com.strataurban.strata.Security.SecurityUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
public interface TransportService {
    Transport addTransport(Transport transport, SecurityUserDetails userDetails);
    List<Transport> getProviderTransports(SecurityUserDetails userDetails);
    Transport getTransportById(Long id);
    Transport updateTransport(Long id, Transport transport, SecurityUserDetails userDetails) throws IllegalAccessException;
    void deleteTransport(Long id);
    List<Transport> getAvailableTransports(SecurityUserDetails userDetails, String transportCategory, Integer capacity);
    Transport updateTransportStatus(Long id, String status);
    Page<Transport> findTransportsByFilters(
            Long providerId,
            String type,
            Integer capacity,
            String description,
            String plateNumber,
            String brand,
            String model,
            String color,
            String state,
            String company,
            Long routeId,
            String status,
            Pageable pageable);
}