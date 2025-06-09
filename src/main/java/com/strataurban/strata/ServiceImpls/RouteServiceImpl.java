package com.strataurban.strata.ServiceImpls;

import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.RouteRepository;
import com.strataurban.strata.Repositories.SupplierRepository;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.Security.SecurityUserDetailsService;
import com.strataurban.strata.Services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
            route.setCity(userDetails.getCity());
            route.setState(userDetails.getState());
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
    public List<Routes> getRoutes(RoutesRequestDTO routesRequestDTO) {

        String country = routesRequestDTO.getCountry();
        String state = routesRequestDTO.getState();
        String city = routesRequestDTO.getCity();

        if (country.isEmpty() || country.isBlank()) {
            return routeRepository.findAll();
        }

        if (state.isEmpty() || state.isBlank()) {
            return routeRepository.findAllByCountry(country);
        }

        if (city.isEmpty() || city.isBlank()) {
            return routeRepository.findAllByCountryAndState(country, state);
        }

        return routeRepository.findAllByCountryAndStateAndCity(country, state, city);
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
        return routeRepository.findAllByProviderId(providerId);
    }


    public List<Routes> getRoutesByLocations(String start, String end) {
        return routeRepository.findByStartContainingAndEndContaining(start, end);
    }

    public Routes addRoute(Routes route) {
        return routeRepository.save(route);
    }

}
