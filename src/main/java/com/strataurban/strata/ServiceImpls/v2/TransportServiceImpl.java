package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.Entities.Providers.Transport;
import com.strataurban.strata.Repositories.v2.TransportRepository;
import com.strataurban.strata.Services.v2.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportServiceImpl implements TransportService {

    private final TransportRepository transportRepository;

    @Autowired
    public TransportServiceImpl(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    @Override
    public Transport addTransport(Transport transport) {
        // Ensure the transport status is set to "Available" by default
        if (transport.getStatus() == null) {
            transport.setStatus("Available");
        }
        return transportRepository.save(transport);
    }

    @Override
    public List<Transport> getProviderTransports(Long providerId) {
        return transportRepository.findByProviderId(providerId);
    }

    @Override
    public Transport getTransportById(Long id) {
        return transportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transport not found with ID: " + id));
    }

    @Override
    public Transport updateTransport(Long id, Transport transport) {
        Transport existingTransport = getTransportById(id);
        // Update fields (only those that are allowed to be updated)
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
        // Status is updated via a separate endpoint, so we don't update it here
        return transportRepository.save(existingTransport);
    }

    @Override
    public void deleteTransport(Long id) {
        Transport transport = getTransportById(id);
        transportRepository.delete(transport);
    }

    @Override
    public List<Transport> getAvailableTransports(Long providerId, String transportCategory, Integer capacity) {
        return transportRepository.findAvailableTransports(providerId, transportCategory, capacity);
    }

    @Override
    public Transport updateTransportStatus(Long id, String status) {
        Transport transport = getTransportById(id);
        // Validate status (you might want to use an enum for status in a production app)
        if (!List.of("Available", "Booked", "In Maintenance").contains(status)) {
            throw new RuntimeException("Invalid status: " + status);
        }
        transport.setStatus(status);
        return transportRepository.save(transport);
    }
}