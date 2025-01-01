package com.strataurban.strata.Services;

import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.Entities.Providers.Routes;

import java.util.List;

public interface RouteService {

    public Routes createRoute(Routes route);
    public Routes getRoute(Long routeId);
    public List<Routes> getRoutes(RoutesRequestDTO routesRequestDTO);
    public Routes updateRoute(RoutesRequestDTO routesRequestDTO);
    public List<Routes> createMultipleRoutes(List<Routes> routes);
    public String deleteRoutes(List<Long> routeIds);
    public String deleteRoute(Long routeId);
}
