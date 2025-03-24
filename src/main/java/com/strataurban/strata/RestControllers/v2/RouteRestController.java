package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Services.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@Tag(name = "Routes", description = "API for managing transportation routes")
public class RouteRestController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/create")
    @Operation(summary = "Create a new route", description = "Creates a new transportation route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route created successfully",
                    content = @Content(schema = @Schema(implementation = Routes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid route data supplied")
    })
    public Routes createRoute(@RequestBody Routes route){
        return routeService.createRoute(route);
    }

    @GetMapping("/get-route")
    @Operation(summary = "Get route by ID", description = "Returns a route based on its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the route"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    public Routes getRoute(
            @Parameter(description = "ID of the route to fetch")
            @RequestParam Long routeId){
        return routeService.getRoute(routeId);
    }

    @GetMapping("/getall")
    @Operation(summary = "Get all routes based on filter criteria", description = "Returns a list of routes matching the request criteria")
    public List<Routes> getRoutes(@RequestBody RoutesRequestDTO requestDTO){
        return routeService.getRoutes(requestDTO);
    }

    @PutMapping("/update")
    @Operation(summary = "Update a route", description = "Updates an existing route with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route updated successfully"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    public Routes updateRoute(@RequestBody RoutesRequestDTO requestDTO){
        return routeService.updateRoute(requestDTO);
    }

    @PostMapping("/create-multiple")
    @Operation(summary = "Create multiple routes", description = "Creates multiple transportation routes in a single request")
    public List<Routes> createMultipleRoutes(@RequestBody List<Routes> routes){
        return routeService.createMultipleRoutes(routes);
    }

    @DeleteMapping("/delete-multiple")
    @Operation(summary = "Delete multiple routes", description = "Deletes multiple routes by their IDs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routes deleted successfully"),
            @ApiResponse(responseCode = "404", description = "One or more routes not found")
    })
    public String deleteMultipleRoutes(@RequestBody List<Long> routeIds){
        return routeService.deleteRoutes(routeIds);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a route", description = "Deletes a route by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    public String deleteRoute(@RequestBody Long routeId){
        return routeService.deleteRoute(routeId);
    }

    @GetMapping("/search")
    @Operation(summary = "Search routes by start and end locations", description = "Returns routes matching the specified start and end locations")
    public ResponseEntity<List<Routes>> findRoutes(
            @Parameter(description = "Starting location") @RequestParam String start,
            @Parameter(description = "Ending location") @RequestParam String end) {
        return ResponseEntity.ok(routeService.getRoutesByLocations(start, end));
    }

    @GetMapping("/get-by-provider")
    @Operation(summary = "Get routes by provider", description = "Returns all routes for a specific provider")
    public ResponseEntity<List<Routes>> getRoutesByProvider(
            @Parameter(description = "ID of the provider") @RequestParam String providerId){
        return ResponseEntity.ok(routeService.getRoutesByProvider(providerId));
    }
}