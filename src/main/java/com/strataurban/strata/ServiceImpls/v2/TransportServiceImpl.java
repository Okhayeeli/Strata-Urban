package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.Transport;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Repositories.v2.TransportRepository;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.Security.SecurityUserDetailsService;
import com.strataurban.strata.Services.v2.TransportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportServiceImpl implements TransportService {

    private final TransportRepository transportRepository;

    private final SecurityUserDetailsService securityUserDetailsService;

    private final ProviderRepository providerRepository;

    public TransportServiceImpl(TransportRepository transportRepository, SecurityUserDetailsService securityUserDetailsService, ProviderRepository providerRepository) {
        this.transportRepository = transportRepository;
        this.securityUserDetailsService = securityUserDetailsService;
        this.providerRepository = providerRepository;
    }

    @Override
    public Transport addTransport(Transport transport, SecurityUserDetails userDetails) {
        if (userDetails.getRole() == EnumRoles.PROVIDER) {
            transport.setProviderId(userDetails.getId());
            Provider provider = providerRepository.findById(userDetails.getId())
                    .orElseThrow(()-> new RuntimeException("Provider with ID " + userDetails.getId() + " not found"));

            if (provider.getTransportCount()==null){
                provider.setTransportCount(0);
            }

            provider.setTransportCount(provider.getTransportCount() + 1);
            transport.setState(provider.getState());
            transport.setCompany(provider.getCompanyName());

        } else if (transport.getProviderId() == null) {
            throw new IllegalArgumentException("Provider ID must be specified for ADMIN or CUSTOMER_SERVICE");
        }
        if (transport.getStatus() == null) {
            transport.setStatus("Available");
        }

        return transportRepository.save(transport);
    }

    @Override
    public List<Transport> getProviderTransports(SecurityUserDetails userDetails) {
        if (userDetails.getRole() == EnumRoles.PROVIDER) {
            return transportRepository.findByProviderId(userDetails.getId());
        }
        return transportRepository.findAll();
    }

    @Override
    public Transport getTransportById(Long id) {
        return transportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transport not found with ID: " + id));
    }

    @Override
    public Transport updateTransport(Long id, Transport transport, SecurityUserDetails userDetails) throws IllegalAccessException {
        Transport existingTransport = getTransportById(id);
        if (userDetails.getRole() == EnumRoles.PROVIDER && !existingTransport.getProviderId().equals(userDetails.getId())) {
            throw new IllegalAccessException("Provider can only update their own transports");
        }
        existingTransport.setType(transport.getType());
        existingTransport.setCapacity(transport.getCapacity());
        existingTransport.setDescription(transport.getDescription());
        existingTransport.setPlateNumber(transport.getPlateNumber());
        existingTransport.setBrand(transport.getBrand());
        existingTransport.setModel(transport.getModel());
        existingTransport.setColor(transport.getColor());
        existingTransport.setState(transport.getState());
        existingTransport.setCompany(transport.getCompany());
        existingTransport.setRouteId(transport.getRouteId());
        if (userDetails.getRole() == EnumRoles.PROVIDER) {
            existingTransport.setProviderId(userDetails.getId());
        } else if (transport.getProviderId() != null) {
            existingTransport.setProviderId(transport.getProviderId());
        }
        return transportRepository.save(existingTransport);
    }

    @Override
    public void deleteTransport(Long id) {
        Transport transport = getTransportById(id);
        transportRepository.delete(transport);
    }

    @Override
    public List<Transport> getAvailableTransports(SecurityUserDetails userDetails, String transportCategory, Integer capacity) {
        if (userDetails.getRole() == EnumRoles.PROVIDER) {
            return transportRepository.findAvailableTransports(userDetails.getId(), transportCategory, capacity);
        }
        return transportRepository.findAvailableTransports(null, transportCategory, capacity);
    }

    @Override
    public Transport updateTransportStatus(Long id, String status) {
        Transport transport = getTransportById(id);
        if (!List.of("Available", "Booked", "In Maintenance").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        transport.setStatus(status);
        return transportRepository.save(transport);
    }

    @Override
    public Page<Transport> findTransportsByFilters(
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
            Pageable pageable) {
        return transportRepository.findByFilters(
                providerId, type, capacity, description, plateNumber, brand, model,
                color, state, company, routeId, status, pageable);
    }
}