package com.strataurban.strata.RestControllers;

import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteRestController {

    @Autowired
    RouteService routeService;

    @PostMapping("/create")
    public Routes createRoute(@RequestBody Routes route){
        return routeService.createRoute(route);
    }

    @GetMapping("/get-route")
    public Routes getRoute(@RequestParam Long routeId){
        return routeService.getRoute(routeId);
    }

    @GetMapping("/getall")
    public List<Routes> getRoutes(@RequestBody RoutesRequestDTO requestDTO){
        return routeService.getRoutes(requestDTO);
    }

    @PutMapping("/update")
    public Routes updateRoute(@RequestBody RoutesRequestDTO requestDTO){
        return routeService.updateRoute(requestDTO);
    }

    @PostMapping("/create-multiple")
    public List<Routes> createMultipleRoutes(@RequestBody List<Routes> routes){
        return routeService.createMultipleRoutes(routes);
    }

    @DeleteMapping("/delete-multiple")
    public String deleteMultipleRoutes(@RequestBody List<Long> routeIds){
       return routeService.deleteRoutes(routeIds);
    }

    @DeleteMapping("/delete")
    public String deleteRoute(@RequestBody Long routeId){
        return routeService.deleteRoute(routeId);
    }
}
