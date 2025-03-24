package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.Entities.Providers.Services;
import com.strataurban.strata.Repositories.v2.ServiceRepository;
import com.strataurban.strata.Services.v2.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public Services addService(Services service) {
        return serviceRepository.save(service);
    }

    @Override
    public List<Services> getProviderServices(Long providerId) {
        return serviceRepository.findByProviderId(providerId);
    }

    @Override
    public Services getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + id));
    }

    @Override
    public Services updateService(Long id, Services service) {
        Services existingService = getServiceById(id);
        existingService.setServiceName(service.getServiceName());
        existingService.setProviderId(service.getProviderId());
        return serviceRepository.save(existingService);
    }

    @Override
    public void deleteService(Long id) {
        Services service = getServiceById(id);
        serviceRepository.delete(service);
    }

    @Override
    public List<Services> getAllServices() {
        return serviceRepository.findAll();
    }
}