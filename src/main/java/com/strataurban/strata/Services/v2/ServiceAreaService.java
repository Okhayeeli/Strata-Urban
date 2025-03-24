package com.strataurban.strata.Services.v2;


import com.strataurban.strata.DTOs.v2.ServiceAreaReportDTO;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ServiceArea;

import java.util.List;

public interface ServiceAreaService {

    // CRUD operations for ServiceArea
    ServiceArea createServiceArea(ServiceArea serviceArea);
    ServiceArea getServiceAreaById(Long id);
    List<ServiceArea> getAllServiceAreas();
    ServiceArea updateServiceArea(Long id, ServiceArea serviceArea);
    void deleteServiceArea(Long id);

    // Operations for managing service areas for a provider
    List<String> getServiceAreasForProvider(Long providerId);
    void addServiceAreasToProvider(Long providerId, List<Long> serviceAreaIds);
    void removeServiceAreasFromProvider(Long providerId, List<Long> serviceAreaIds);

    // Retrieve providers in a specific service area
    List<Provider> getProvidersInServiceArea(Long serviceAreaId);

    // Report: Service areas with provider counts and lists
    List<ServiceAreaReportDTO> getServiceAreaReport();
}