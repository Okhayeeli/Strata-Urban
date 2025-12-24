package com.strataurban.strata.Services;

import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.DTOs.v2.RouteWithProvidersDTO;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Security.SecurityUserDetails;

import java.util.List;

public interface RouteService {

    Routes createRoute(Routes route);
    Routes getRoute(Long routeId);
    List<Routes> getRoutes(String country, String state, String city, SecurityUserDetails userDetails);
    Routes updateRoute(RoutesRequestDTO routesRequestDTO);
    List<Routes> createMultipleRoutes(List<Routes> routes);
    String deleteRoutes(List<Long> routeIds);
    String deleteRoute(Long routeId);
    List<Routes> getRoutesByProvider(String providerId);
    List<Routes> getRoutesByLocations(String start, String end);
    Routes addProviderToRoute(Long routeId, String providerId);
    RouteWithProvidersDTO getRouteWithProviders(Long routeId);
    Routes toggleRoute(Long routeId);
    Routes removeProviderToRoute(Long routeId, String providerToRemove);
     Long countEnabledRoutes();
     Long countActiveCountries();
     List<Routes> searchRoutes(String search);
     Routes disableRoute(Long routeId);
     Routes enableRoute(Long routeId);
}
