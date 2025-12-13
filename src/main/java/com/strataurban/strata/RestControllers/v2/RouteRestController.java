package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.RoutesRequestDTO;
import com.strataurban.strata.DTOs.v2.RouteWithProvidersDTO;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @ApiResponse(responseCode = "400", description = "Invalid route data supplied"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER or ADMIN can create a route. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('PROVIDER') or hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public Routes createRoute(@RequestBody Routes route) {
        try {
            return routeService.createRoute(route);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER or ADMIN can create a route. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping("/get-route")
    @Operation(summary = "Get route by ID", description = "Returns a route based on its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the route"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if authorized), DRIVER, ADMIN, or DEVELOPER can access a route. CLIENT and others are restricted.")
    })
//    @PreAuthorize("hasRole('PROVIDER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public Routes getRoute(
            @Parameter(description = "ID of the route to fetch")
            @RequestParam Long routeId) {
        try {
            return routeService.getRoute(routeId);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if authorized), DRIVER, ADMIN, or DEVELOPER can access a route. CLIENT and others are restricted.");
        }
    }

    @GetMapping("/getall")
    @Operation(summary = "Get all routes based on filter criteria", description = "Returns a list of routes matching the request criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER, DRIVER, ADMIN, or DEVELOPER can get all routes. CLIENT and others are restricted.")
    })
    @PreAuthorize("hasRole('PROVIDER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public List<Routes> getRoutes(@RequestBody RoutesRequestDTO requestDTO) {
        try {
            return routeService.getRoutes(requestDTO);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER, DRIVER, ADMIN, or DEVELOPER can get all routes. CLIENT and others are restricted.");
        }
    }

    @PutMapping("/update")
    @Operation(summary = "Update a route", description = "Updates an existing route with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route updated successfully"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if authorized) or ADMIN can update a route. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })

    public Routes updateRoute(@RequestBody RoutesRequestDTO requestDTO) {
        try {
            return routeService.updateRoute(requestDTO);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if authorized) or ADMIN can update a route. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @PostMapping("/create-multiple")
    @Operation(summary = "Create multiple routes", description = "Creates multiple transportation routes in a single request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routes created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER or ADMIN can create multiple routes. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public List<Routes> createMultipleRoutes(@RequestBody List<Routes> routes) {
        try {
            return routeService.createMultipleRoutes(routes);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER or ADMIN can create multiple routes. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @DeleteMapping("/delete-multiple")
    @Operation(summary = "Delete multiple routes", description = "Deletes multiple routes by their IDs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routes deleted successfully"),
            @ApiResponse(responseCode = "404", description = "One or more routes not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if authorized) or ADMIN can delete multiple routes. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public String deleteMultipleRoutes(@RequestBody List<Long> routeIds) {
        try {
            return routeService.deleteRoutes(routeIds);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if authorized) or ADMIN can delete multiple routes. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a route", description = "Deletes a route by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if authorized) or ADMIN can delete a route. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('PROVIDER')  or hasRole('ADMIN')")
    public String deleteRoute(@RequestBody Long routeId) {
        try {
            return routeService.deleteRoute(routeId);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if authorized) or ADMIN can delete a route. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search routes by start and end locations", description = "Returns routes matching the specified start and end locations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT, PROVIDER, DRIVER, ADMIN, or DEVELOPER can search routes. Others are restricted.")
    })
//    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<Routes>> findRoutes(
            @Parameter(description = "Starting location") @RequestParam String start,
            @Parameter(description = "Ending location") @RequestParam String end) {
        try {
            return ResponseEntity.ok(routeService.getRoutesByLocations(start, end));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT, PROVIDER, DRIVER, ADMIN, or DEVELOPER can search routes. Others are restricted.");
        }
    }

    @GetMapping("/get-by-provider")
    @Operation(summary = "Get routes by provider", description = "Returns all routes for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id matches providerId), DRIVER, ADMIN, or DEVELOPER can access routes by provider. CLIENT and others are restricted.")
    })
//    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #providerId) or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<Routes>> getRoutesByProvider(
            @Parameter(description = "ID of the provider") @RequestParam String providerId) {
        try {
            return ResponseEntity.ok(routeService.getRoutesByProvider(providerId));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id matches providerId), DRIVER, ADMIN, or DEVELOPER can access routes by provider. CLIENT and others are restricted.");
        }
    }

    @PostMapping("/{routeId}/add-provider")
    @Operation(summary = "Add a provider to a route", description = "Appends a provider to the list of providers for a specific route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider added successfully",
                    content = @Content(schema = @Schema(implementation = Routes.class))),
            @ApiResponse(responseCode = "404", description = "Route or Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN, CUSTOMER_SERVICE, or the PROVIDER themselves can add providers to routes")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE') or (hasRole('PROVIDER') and principal.id == #providerId)")
    public ResponseEntity<Routes> addProviderToRoute(
            @Parameter(description = "ID of the route") @PathVariable Long routeId,
            @Parameter(description = "ID of the provider to add") @RequestParam String providerId) {

        Routes updatedRoute = routeService.addProviderToRoute(routeId, providerId);
        return ResponseEntity.ok(updatedRoute);
    }

    @GetMapping("/get-route-providers")
    @Operation(summary = "Get route by ID with provider details")
    public ResponseEntity<RouteWithProvidersDTO> getRouteProviders(@RequestParam Long routeId) {
        RouteWithProvidersDTO dto = routeService.getRouteWithProviders(routeId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/toggle")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE')")
    public ResponseEntity<Routes> toggleRoute(@RequestParam Long routeId) {
        return ResponseEntity.ok(routeService.toggleRoute(routeId));
    }

    @PutMapping("/{routeId}/remove-provider")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER_SERVICE')")
    public ResponseEntity<Routes> removeProviderFromRoute(@PathVariable Long routeId, @RequestParam String providerId) {
        Routes updatedRoute = routeService.removeProviderToRoute(routeId, providerId);
        return ResponseEntity.ok(updatedRoute);
    }
}