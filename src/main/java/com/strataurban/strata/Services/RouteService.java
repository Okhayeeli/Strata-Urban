package com.strataurban.strata.Services;

import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.DTOs.v2.RouteWithProvidersDTO;
import com.strataurban.strata.Entities.Providers.Routes;

import java.util.List;

public interface RouteService {

    Routes createRoute(Routes route);
    Routes getRoute(Long routeId);
    List<Routes> getRoutes(RoutesRequestDTO routesRequestDTO);
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
}
