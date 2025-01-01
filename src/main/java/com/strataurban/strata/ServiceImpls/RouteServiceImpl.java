package com.strataurban.strata.ServiceImpls;

import com.querydsl.core.BooleanBuilder;
import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Repositories.RouteRepository;
import com.strataurban.strata.Repositories.SupplierRepository;
import com.strataurban.strata.Services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    SupplierRepository supplierRepository;


    @Override
    public Routes createRoute(Routes route) {
        return routeRepository.save(route);
    }

    @Override
    public Routes getRoute(Long routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(()-> new RuntimeException("Route not found"));
    }

    @Override
    public List<Routes> getRoutes(RoutesRequestDTO routesRequestDTO) {

//        QRoutes qRoutes=QRoutes.routes;
//        BooleanBuilder booleanBuilder = new BooleanBuilder();
//
//        booleanBuilder.and(qRoutes.country.eq("Nigeria"));
//        booleanBuilder.and(qRoutes.state.eq("Bayelsa"));

        String country = routesRequestDTO.getCountry();
        String state = routesRequestDTO.getState();
        String city = routesRequestDTO.getCity();

        if(country.isEmpty() || country.isBlank()){
            return routeRepository.findAll();
        }

        if (state.isEmpty() || state.isBlank()){
            return routeRepository.findAllByCountry(country);
        }

        if (city.isEmpty() || city.isBlank()){
            return routeRepository.findAllByCountryAndState(country, state);
        }

        return routeRepository.findAllByCountryAndStateAndCity(country, state, city);
    }

    @Override
    public Routes updateRoute(RoutesRequestDTO routesRequestDTO) {
        Routes existingRoute = routeRepository.findById(routesRequestDTO.getId())
                .orElseThrow(()-> new RuntimeException("Route not found"));

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
}
