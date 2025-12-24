package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Repositories.SupplierRepository;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Repositories.v2.RouteRepository;
import com.strataurban.strata.ServiceImpls.RouteServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminRouteController {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteServiceImpl routeService;

    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProviderRepository providerRepository;

    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Display routes management page with filters
     */
    @GetMapping("/routes")
    public String showRoutes(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Specification<Routes> spec = Specification.where(null);

            if (country != null && !country.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("country"), country)
                );
            }

            if (state != null && !state.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("state"), state)
                );
            }

            if (city != null && !city.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%")
                );
            }

            if (search != null && !search.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("start")), "%" + search.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("end")), "%" + search.toLowerCase() + "%")
                        )
                );
            }

            if (enabled != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("isEnabled"), enabled)
                );
            }

            Page<Routes> routes = routeRepository.findAll(spec, pageable);

            // Get provider counts for each route
            Map<Long, Integer> providerCounts = new HashMap<>();
            Map<Long, List<Map<String, String>>> routeProviders = new HashMap<>();

            for (Routes route : routes.getContent()) {
                List<String> providerIds = route.getProviderIdList();
                providerCounts.put(route.getId(), providerIds.size());

                List<Map<String, String>> providers = providerIds.stream()
                        .map(id -> {
                            try {
                                Provider provider = providerRepository.findById(Long.parseLong(id)).orElse(null);
                                if (provider != null) {
                                    Map<String, String> providerInfo = new HashMap<>();
                                    providerInfo.put("id", id);
                                    providerInfo.put("name", provider.getCompanyName());
                                    return providerInfo;
                                }
                            } catch (Exception e) {
                                // Skip invalid provider IDs
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                routeProviders.put(route.getId(), providers);
            }

            model.addAttribute("routes", routes);
            model.addAttribute("providerCounts", providerCounts);
            model.addAttribute("routeProviders", routeProviders);

            // Statistics
            model.addAttribute("totalRoutes", routeRepository.count());
            model.addAttribute("enabledRoutes", routeRepository.countEnabledRoutes());
            model.addAttribute("activeCountries", routeRepository.countActiveCountries());

            // Calculate average price
            List<Routes> allRoutes = routeRepository.findAll();
            double avgPrice = allRoutes.stream()
                    .filter(r -> r.getPrice() != null)
                    .mapToDouble(r -> r.getPrice().doubleValue())
                    .average()
                    .orElse(0.0);
            model.addAttribute("averagePrice", String.format("%.2f", avgPrice));

            // Get distinct values for filters
            model.addAttribute("countries", routeRepository.findAll().stream()
                    .map(Routes::getCountry)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList()));

        } catch (Exception e) {
            System.err.println("Error fetching routes: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("routes", Page.empty());
            model.addAttribute("totalRoutes", 0);
            model.addAttribute("enabledRoutes", 0);
            model.addAttribute("activeCountries", 0);
            model.addAttribute("averagePrice", "0.00");
        }

        return "admin/routes";
    }

    /**
     * View single route details with providers
     */
    @GetMapping("/routes/{id}")
    public String viewRoute(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            Routes route = routeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Route not found"));

            // Get provider details
            List<Map<String, Object>> providers = route.getProviderIdList().stream()
                    .map(providerId -> {
                        try {
                            Provider provider = providerRepository.findById(Long.parseLong(providerId)).orElse(null);
                            if (provider != null) {
                                Map<String, Object> providerInfo = new HashMap<>();
                                providerInfo.put("id", provider.getId());
                                providerInfo.put("companyName", provider.getCompanyName());
                                providerInfo.put("email", provider.getCompanyBusinessEmail());
                                providerInfo.put("phone", provider.getCompanyBusinessPhone());
                                providerInfo.put("rating", provider.getRating());
                                providerInfo.put("city", provider.getCity());
                                return providerInfo;
                            }
                        } catch (Exception e) {
                            System.err.println("Error fetching provider: " + e.getMessage());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            model.addAttribute("route", route);
            model.addAttribute("providers", providers);

            return "admin/route-detail";
        } catch (Exception e) {
            System.err.println("Error fetching route: " + e.getMessage());
            return "redirect:/admin/routes?error=route_not_found";
        }
    }

    /**
     * Show create route form
     */
    @GetMapping("/routes/create")
    public String createRouteForm(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        // Get all providers for selection
        List<Provider> providers = supplierRepository.findAll();
        model.addAttribute("providers", providers);

        return "admin/route-create";
    }

    /**
     * Create new route
     */
    @PostMapping("/routes/create")
    public String createRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam BigDecimal price,
            @RequestParam String country,
            @RequestParam String state,
            @RequestParam String city,
            @RequestParam(required = false) List<String> providerIds,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            Routes route = new Routes();
            route.setStart(start);
            route.setEnd(end);
            route.setPrice(price);
            route.setCountry(country);
            route.setState(state);
            route.setCity(city);
            route.setIsEnabled(true);

            if (providerIds != null && !providerIds.isEmpty()) {
                route.setProviderIdList(providerIds);
            }

            Routes savedRoute = routeRepository.save(route);
            return "redirect:/admin/routes/" + savedRoute.getId() + "?success=created";
        } catch (Exception e) {
            System.err.println("Error creating route: " + e.getMessage());
            return "redirect:/admin/routes/create?error=creation_failed";
        }
    }

    /**
     * Show edit route form
     */
    @GetMapping("/routes/{id}/edit")
    public String editRouteForm(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            Routes route = routeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Route not found"));
            model.addAttribute("route", route);

            List<Provider> allProviders = supplierRepository.findAll();
            model.addAttribute("providers", allProviders);

            List<String> selectedProviderIds = route.getProviderIdList();
            model.addAttribute("selectedProviderIds", selectedProviderIds);

            return "admin/route-edit";
        } catch (Exception e) {
            System.err.println("Error fetching route for edit: " + e.getMessage());
            return "redirect:/admin/routes?error=route_not_found";
        }
    }

    /**
     * Update route
     */
    @PostMapping("/routes/{id}/update")
    public String updateRoute(
            @PathVariable Long id,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam BigDecimal price,
            @RequestParam String country,
            @RequestParam String state,
            @RequestParam String city,
            @RequestParam(required = false) List<String> providerIds,
            @RequestParam(required = false) Boolean enabled,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            Routes route = routeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Route not found"));

            route.setStart(start);
            route.setEnd(end);
            route.setPrice(price);
            route.setCountry(country);
            route.setState(state);
            route.setCity(city);
            route.setIsEnabled(enabled != null ? enabled : true);

            if (providerIds != null) {
                route.setProviderIdList(providerIds);
            } else {
                route.setProviderIdList(new ArrayList<>());
            }

            routeRepository.save(route);
            return "redirect:/admin/routes/" + id + "?success=updated";
        } catch (Exception e) {
            System.err.println("Error updating route: " + e.getMessage());
            return "redirect:/admin/routes/" + id + "/edit?error=update_failed";
        }
    }

    /**
     * Toggle route enabled status
     */
    @PostMapping("/routes/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleRoute(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            Routes route = routeService.toggleRoute(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Route status updated",
                    "enabled", route.getIsEnabled()
            ));
        } catch (Exception e) {
            System.err.println("Error toggling route: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to toggle route"));
        }
    }

    /**
     * Delete route
     */
    @PostMapping("/routes/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteRoute(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            routeRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Route deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting route: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete route"));
        }
    }

    /**
     * Add provider to route
     */
    @PostMapping("/routes/{id}/providers/add")
    @ResponseBody
    public ResponseEntity<?> addProviderToRoute(
            @PathVariable Long id,
            @RequestParam String providerId,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            Routes route = routeService.addProviderToRoute(id, providerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Provider added successfully",
                    "providerCount", route.getProviderIdList().size()
            ));
        } catch (Exception e) {
            System.err.println("Error adding provider to route: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove provider from route
     */
    @PostMapping("/routes/{id}/providers/remove")
    @ResponseBody
    public ResponseEntity<?> removeProviderFromRoute(
            @PathVariable Long id,
            @RequestParam String providerId,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            Routes route = routeService.removeProviderToRoute(id, providerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Provider removed successfully",
                    "providerCount", route.getProviderIdList().size()
            ));
        } catch (Exception e) {
            System.err.println("Error removing provider from route: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get routes by provider
     */
    @GetMapping("/routes/by-provider/{providerId}")
    @ResponseBody
    public ResponseEntity<?> getRoutesByProvider(
            @PathVariable String providerId,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            List<Routes> routes = routeService.getRoutesForProvider(providerId);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch routes"));
        }
    }

    /**
     * Bulk operations - enable/disable multiple routes
     */
    @PostMapping("/routes/bulk/toggle")
    @ResponseBody
    public ResponseEntity<?> bulkToggleRoutes(
            @RequestParam List<Long> routeIds,
            @RequestParam Boolean enable,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            int updated = 0;
            for (Long routeId : routeIds) {
                try {
                    Routes route = routeRepository.findById(routeId).orElse(null);
                    if (route != null) {
                        route.setIsEnabled(enable);
                        routeRepository.save(route);
                        updated++;
                    }
                } catch (Exception e) {
                    System.err.println("Error updating route " + routeId + ": " + e.getMessage());
                }
            }
            return ResponseEntity.ok(Map.of(
                    "message", updated + " routes updated",
                    "updated", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to bulk update"));
        }
    }

    /**
     * Get route statistics
     */
    @GetMapping("/routes/stats")
    @ResponseBody
    public ResponseEntity<?> getRouteStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            List<Routes> allRoutes = routeRepository.findAll();

            Map<String, Long> routesByCountry = allRoutes.stream()
                    .collect(Collectors.groupingBy(Routes::getCountry, Collectors.counting()));

            Map<String, Long> routesByState = allRoutes.stream()
                    .collect(Collectors.groupingBy(Routes::getState, Collectors.counting()));

            return ResponseEntity.ok(Map.of(
                    "totalRoutes", routeRepository.count(),
                    "enabledRoutes", routeRepository.countEnabledRoutes(),
                    "activeCountries", routeRepository.countActiveCountries(),
                    "routesByCountry", routesByCountry,
                    "routesByState", routesByState,
                    "averageProvidersPerRoute", allRoutes.stream()
                            .mapToInt(r -> r.getProviderIdList().size())
                            .average()
                            .orElse(0.0)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch statistics"));
        }
    }
}