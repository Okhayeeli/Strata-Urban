package com.strataurban.strata.ServiceImpls;

import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.DTOs.v2.ProviderSummaryDTO;
import com.strataurban.strata.DTOs.v2.RouteWithProvidersDTO;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.RouteRepository;
import com.strataurban.strata.Repositories.SupplierRepository;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.Security.SecurityUserDetailsService;
import com.strataurban.strata.Services.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RouteServiceImpl implements RouteService {

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    SecurityUserDetailsService securityUserDetailsService;


    @Override
    public Routes createRoute(Routes route) {

        SecurityUserDetails userDetails = securityUserDetailsService.getSecurityUserDetails();

        if (userDetails.getRole() == EnumRoles.PROVIDER){
            route.setProviderId(String.valueOf(userDetails.getId()));
            route.setCountry(userDetails.getCountry());
        }

        if (route.getStart() == null || route.getEnd() == null) {
            throw new IllegalArgumentException("Start and End locations must be specified");
        }

        if (route.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (route.getCity() == null || route.getState() == null || route.getCountry() == null) {
            throw new IllegalArgumentException("City, State, and Country must be specified");
        }

        if (route.getStart().isEmpty() || route.getEnd().isEmpty()) {
            throw new IllegalArgumentException("Start and End locations cannot be empty");
        }

        if (route.getStart().equals(route.getEnd())) {
            throw new IllegalArgumentException("Start and End locations cannot be the same");
        }

        if (route.getProviderId() == null && userDetails.getRole() != EnumRoles.ADMIN && userDetails.getRole() != EnumRoles.CUSTOMER_SERVICE) {
            throw new IllegalArgumentException("Provider ID must be specified for ADMIN or CUSTOMER_SERVICE roles");
        }

        if (route.getProviderId() == null) {
            throw new IllegalArgumentException("Provider ID must be specified");
        }
        return routeRepository.save(route);
    }

    @Override
    public Routes getRoute(Long routeId) {
        return routeRepository.findById(routeId).orElseThrow(() -> new RuntimeException("Route not found"));
    }

    @Override
    public List<Routes> getRoutes(RoutesRequestDTO routesRequestDTO,
                                  SecurityUserDetails userDetails) {

        boolean isAdmin = userDetails.getRole() == EnumRoles.ADMIN;

        String country = isAdmin
                ? routesRequestDTO.getCountry()
                : userDetails.getCountry();

        String state = routesRequestDTO.getState();
        String city = routesRequestDTO.getCity();

        // Admin with no country → return everything
        if (isAdmin && isBlank(country)) {
            return routeRepository.findAll();
        }

        if (isBlank(state)) {
            return routeRepository.findAllByCountryAndIsEnabledTrue(country);
        }

        if (isBlank(city)) {
            return routeRepository.findAllByCountryAndStateAndIsEnabledTrue(country, state);
        }

        return routeRepository.findAllByCountryAndStateAndCityAndIsEnabledTrue(
                country, state, city
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }


    @Override
    public Routes updateRoute(RoutesRequestDTO routesRequestDTO) {
        Routes existingRoute = routeRepository.findById(routesRequestDTO.getId()).orElseThrow(() -> new RuntimeException("Route not found"));

        existingRoute.setStart(routesRequestDTO.getStart());
        existingRoute.setEnd(routesRequestDTO.getEnd());
        existingRoute.setPrice(routesRequestDTO.getPrice());
        existingRoute.setCity(routesRequestDTO.getCity());
        existingRoute.setState(routesRequestDTO.getState());
        existingRoute.setCountry(routesRequestDTO.getCountry());

        routeRepository.save(existingRoute);
        return routeRepository.save(existingRoute);
    }

    @Override
    public List<Routes> createMultipleRoutes(List<Routes> routes) {
        return routeRepository.saveAll(routes);
    }

    @Override
    public String deleteRoutes(List<Long> routeIds) {
        routeRepository.deleteAllById(routeIds);
        return "Successfully deleted";
    }

    @Override
    public String deleteRoute(Long routeId) {
        routeRepository.deleteById(routeId);
        return "Successfully deleted";
    }

    @Override
    public List<Routes> getRoutesByProvider(String providerId) {
        return routeRepository.findAllByProviderIdAndIsEnabledTrue(providerId);
    }


    public List<Routes> getRoutesByLocations(String start, String end) {
        return routeRepository.findByStartContainingAndEndContainingAndIsEnabledTrue(start, end);
    }

    public Routes addRoute(Routes route) {
        return routeRepository.save(route);
    }

    @Override
    public Routes addProviderToRoute(Long routeId, String providerIdToAdd) {
        Routes route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + routeId));

        // Verify provider exists
        boolean providerExists = supplierRepository.existsById(Long.parseLong(providerIdToAdd));
        if (!providerExists) {
            throw new RuntimeException("Provider not found with id: " + providerIdToAdd);
        }

        route.addProviderId(providerIdToAdd);
        return routeRepository.save(route);
    }

    @Override
    public Routes removeProviderToRoute(Long routeId, String providerToRemove){
        Routes route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + routeId));

        boolean providerExists = supplierRepository.existsById(Long.parseLong(providerToRemove));
        if (!providerExists) {
            throw new RuntimeException("Provider not found with id: " + providerToRemove);
        }
        route.removeProviderId(providerToRemove);
        return routeRepository.save(route);
    }

    @Override
    public Routes toggleRoute(Long routeId){
        Routes route = routeRepository.findById(routeId).orElseThrow(() -> new RuntimeException("Route not found"));
        route.setIsEnabled(!route.getIsEnabled());
        return routeRepository.save(route);
    }

    @Override
    public RouteWithProvidersDTO getRouteWithProviders(Long routeId) {
        Routes route = getRoute(routeId);

        List<ProviderSummaryDTO> providers = route.getProviderIdList().stream()
                .map(id -> supplierRepository.findById(Long.parseLong(id))
                        .map(p -> new ProviderSummaryDTO(
                                String.valueOf(p.getId()),
                                p.getCompanyName(),
                                p.getCompanyLogoUrl(),
                                p.getCompanyBusinessPhone(),
                                p.getCompanyBusinessEmail(),
                                p.getRating()
                        ))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        RouteWithProvidersDTO dto = new RouteWithProvidersDTO();
        dto.setId(route.getId());
        dto.setStart(route.getStart());
        dto.setEnd(route.getEnd());
        dto.setPrice(route.getPrice());
        dto.setState(route.getState());
        dto.setCountry(route.getCountry());
        dto.setCity(route.getCity());
        dto.setProviders(providers);

        return dto;
    }



    /**
     * Get all route IDs that a provider is assigned to
     *
     * @param providerId The provider's ID
     * @return List of route IDs (empty list if none found)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "providerRoutes", key = "#providerId")
    public List<Long> getRouteIdsForProvider(String providerId) {
        if (providerId == null || providerId.trim().isEmpty()) {
            log.warn("Attempted to get routes for null/empty provider ID");
            return Collections.emptyList();
        }

        log.debug("Fetching route IDs for provider: {}", providerId);

        List<Long> routeIds = routeRepository.findRouteIdsByProviderId(providerId.trim());

        log.info("Found {} route(s) for provider {}", routeIds.size(), providerId);
        return routeIds;
    }

    /**
     * Get all route IDs for a provider (using Long provider ID)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "providerRoutes", key = "#providerId")
    public List<Long> getRouteIdsForProvider(Long providerId) {
        if (providerId == null) {
            log.warn("Attempted to get routes for null provider ID");
            return Collections.emptyList();
        }

        return getRouteIdsForProvider(String.valueOf(providerId));
    }

    /**
     * Get full Route objects for a provider
     */
    @Transactional(readOnly = true)
    public List<Routes> getRoutesForProvider(String providerId) {
        if (providerId == null || providerId.trim().isEmpty()) {
            log.warn("Attempted to get routes for null/empty provider ID");
            return Collections.emptyList();
        }

        log.debug("Fetching full route objects for provider: {}", providerId);

        List<Routes> routes = routeRepository.findRoutesByProviderId(providerId.trim());

        log.info("Found {} route(s) for provider {}", routes.size(), providerId);
        return routes;
    }

    /**
     * Get routes for a provider filtered by location
     */
    @Transactional(readOnly = true)
    public List<Routes> getRoutesForProviderByLocation(
            String providerId,
            String country,
            String state,
            String city) {

        if (providerId == null || providerId.trim().isEmpty()) {
            log.warn("Attempted to get routes for null/empty provider ID");
            return Collections.emptyList();
        }

        log.debug("Fetching routes for provider {} in location: country={}, state={}, city={}",
                providerId, country, state, city);

        List<Routes> routes = routeRepository.findRoutesByProviderIdAndLocation(
                providerId.trim(), country, state, city);

        log.info("Found {} route(s) for provider {} in specified location", routes.size(), providerId);
        return routes;
    }

    /**
     * Check if a provider is assigned to a specific route
     */
    @Transactional(readOnly = true)
    public boolean isProviderAssignedToRoute(Long routeId, String providerId) {
        if (routeId == null || providerId == null || providerId.trim().isEmpty()) {
            return false;
        }

        Routes route = routeRepository.findById(routeId).orElse(null);

        if (route == null) {
            return false;
        }

        List<String> providerIds = route.getProviderIdList();
        return providerIds.contains(providerId.trim());
    }

    /**
     * Check if a provider has any routes assigned
     */
    @Transactional(readOnly = true)
    public boolean hasRoutes(String providerId) {
        if (providerId == null || providerId.trim().isEmpty()) {
            return false;
        }

        List<Long> routeIds = getRouteIdsForProvider(providerId);
        return !routeIds.isEmpty();
    }
}
