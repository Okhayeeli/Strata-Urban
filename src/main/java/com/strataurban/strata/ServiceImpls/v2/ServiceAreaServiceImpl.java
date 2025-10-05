package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.DTOs.v2.ServiceAreaReportDTO;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ServiceArea;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Repositories.v2.ServiceAreaRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.Services.v2.ServiceAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServiceAreaServiceImpl implements ServiceAreaService {

    private final ServiceAreaRepository serviceAreaRepository;
    private final UserRepository userRepository;

    @Autowired
    public ServiceAreaServiceImpl(ServiceAreaRepository serviceAreaRepository, UserRepository userRepository) {
        this.serviceAreaRepository = serviceAreaRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ServiceArea createServiceArea(ServiceArea serviceArea) {
        return serviceAreaRepository.save(serviceArea);
    }

    @Override
    public ServiceArea getServiceAreaById(Long id) {
        return serviceAreaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service area not found with ID: " + id));
    }

    @Override
    public List<ServiceArea> getAllServiceAreas() {
        return serviceAreaRepository.findAll();
    }

    @Override
    public ServiceArea updateServiceArea(Long id, ServiceArea serviceArea) {
        ServiceArea existingServiceArea = getServiceAreaById(id);
        existingServiceArea.setName(serviceArea.getName());
        existingServiceArea.setDescription(serviceArea.getDescription());
        return serviceAreaRepository.save(existingServiceArea);
    }

    @Override
    public void deleteServiceArea(Long id) {
        ServiceArea serviceArea = getServiceAreaById(id);
        serviceAreaRepository.delete(serviceArea);
    }

    @Override
    public List<String> getServiceAreasForProvider(Long providerId) {
        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));
        if (!(user instanceof Provider)) {
            throw new RuntimeException("User with ID " + providerId + " is not a provider");
        }
        Provider provider = (Provider) user;

        String serviceAreas = provider.getServiceAreas();
        if (serviceAreas == null || serviceAreas.trim().isEmpty()) {
            return Collections.emptyList(); // Return an empty list if no service areas exist
        }

        // Split the comma-separated string and convert to a list of strings
        return Arrays.stream(serviceAreas.split(","))
                .map(String::trim) // Remove any whitespace around IDs
                .filter(id -> !id.isEmpty()) // Filter out empty entries
                .collect(Collectors.toList());
    }
    @Override
    public void addServiceAreasToProvider(Long providerId, List<Long> serviceAreaIds) {
        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));
        if (!(user instanceof Provider)) {
            throw new RuntimeException("User with ID " + providerId + " is not a provider");
        }
        Provider provider = (Provider) user;

        List<ServiceArea> serviceAreas = serviceAreaRepository.findAllById(serviceAreaIds);
        if (serviceAreas.size() != serviceAreaIds.size()) {
            throw new RuntimeException("One or more service area IDs are invalid");
        }

        // Get the existing serviceAreas string
        String existingServiceAreas = provider.getServiceAreas();

        // Get the new service area names to add
        List<String> newServiceAreaNames = serviceAreas.stream()
                .map(ServiceArea::getName)
                .toList();

        // If there’s no existing string, just join the new names
        if (existingServiceAreas == null || existingServiceAreas.isEmpty()) {
            String serviceAreasString = String.join(",", newServiceAreaNames);
            provider.setServiceAreas(serviceAreasString);
        } else {
            // Split existing names into a set to check for duplicates
            Set<String> existingNames = new HashSet<>(Arrays.asList(existingServiceAreas.split(",")));

            // Filter out names that already exist
            String serviceAreasString = newServiceAreaNames.stream()
                    .filter(name -> !existingNames.contains(name)) // Skip duplicates
                    .collect(Collectors.joining(","));

            // Append only if there are new names to add
            String updatedServiceAreas = serviceAreasString.isEmpty()
                    ? existingServiceAreas
                    : existingServiceAreas + "," + serviceAreasString;
            provider.setServiceAreas(updatedServiceAreas);
        }

        userRepository.save(provider);
    }


    @Override
    public void removeServiceAreasFromProvider(Long providerId, List<Long> serviceAreaIds) {
        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));
        if (!(user instanceof Provider)) {
            throw new RuntimeException("User with ID " + providerId + " is not a provider");
        }
        Provider provider = (Provider) user;

        // Get the current serviceAreas string
        String currentServiceAreas = provider.getServiceAreas();
        if (currentServiceAreas == null || currentServiceAreas.isEmpty()) {
            return; // Nothing to remove if the string is null or empty
        }

        // Fetch ServiceArea entities to get their names
        List<ServiceArea> serviceAreas = serviceAreaRepository.findAllById(serviceAreaIds);
        if (serviceAreas.size() != serviceAreaIds.size()) {
            throw new RuntimeException("One or more service area IDs are invalid");
        }

        // Get the list of names to remove
        List<String> namesToRemove = serviceAreas.stream()
                .map(ServiceArea::getName)
                .toList();

        // Split the current serviceAreas string into a mutable list
        List<String> serviceAreaNameList = new ArrayList<>(Arrays.asList(currentServiceAreas.split(",")));

        // Remove the specified names
        serviceAreaNameList.removeAll(namesToRemove);

        // Join the remaining names back into a comma-separated string
        String updatedServiceAreas = String.join(",", serviceAreaNameList);

        // Update the provider's serviceAreas field
        provider.setServiceAreas(updatedServiceAreas.isEmpty() ? null : updatedServiceAreas);

        // Save the updated provider
        userRepository.save(provider);
    }


    @Override
    public List<Provider> getProvidersInServiceArea(Long serviceAreaId) {
        // Fetch the ServiceArea to get its name and validate existence
        ServiceArea serviceArea = getServiceAreaById(serviceAreaId);
        String serviceAreaName = serviceArea.getName();

        // Use the custom query to fetch providers containing this service area name
        List<User> providers = userRepository.findProvidersByServiceAreaName(serviceAreaName);

        // Cast to Provider (safe due to TYPE(u) = Provider in the query)
        return providers.stream()
                .map(user -> (Provider) user)
                .collect(Collectors.toList());

    }

    @Override
    public List<ServiceAreaReportDTO> getServiceAreaReport() {
        List<ServiceArea> serviceAreas = getAllServiceAreas();

        return serviceAreas.stream().map(sa -> {
            // Use the custom query to get providers for this service area
            List<User> providerUsers = userRepository.findProvidersByServiceAreaId((sa.getName()));
            List<Provider> providers = providerUsers.stream()
                    .map(user -> (Provider) user)
                    .collect(Collectors.toList());

            return new ServiceAreaReportDTO(
                    sa.getId(),
                    sa.getName(),
                    providers.size(),
                    providers,
                    sa.getDescription()
            );
        }).collect(Collectors.toList());
    }
}