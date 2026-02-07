package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.Transport;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Repositories.v2.TransportRepository;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminVehicleController {

    @Autowired
    private TransportRepository transportRepository;

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
     * Display vehicles management page with provider grouping
     */
    @GetMapping("/vehicles")
    public String showVehicles(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        // Check authentication
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        // Get admin user from session
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            // Get all vehicles
            List<Transport> allVehicles = transportRepository.findAll();

            // Apply filters
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                allVehicles = allVehicles.stream()
                        .filter(vehicle ->
                                (vehicle.getBrand() != null && vehicle.getBrand().toLowerCase().contains(searchLower)) ||
                                (vehicle.getModel() != null && vehicle.getModel().toLowerCase().contains(searchLower)) ||
                                (vehicle.getPlateNumber() != null && vehicle.getPlateNumber().toLowerCase().contains(searchLower)) ||
                                (vehicle.getColor() != null && vehicle.getColor().toLowerCase().contains(searchLower))
                        )
                        .collect(Collectors.toList());
            }

            if (type != null && !type.isEmpty()) {
                allVehicles = allVehicles.stream()
                        .filter(vehicle -> type.equalsIgnoreCase(vehicle.getType()))
                        .collect(Collectors.toList());
            }

            if (status != null && !status.isEmpty()) {
                allVehicles = allVehicles.stream()
                        .filter(vehicle -> status.equalsIgnoreCase(vehicle.getStatus()))
                        .collect(Collectors.toList());
            }

            if (providerId != null) {
                allVehicles = allVehicles.stream()
                        .filter(vehicle -> vehicle.getProviderId() != null &&
                                vehicle.getProviderId().equals(providerId))
                        .collect(Collectors.toList());
            }

            // Group vehicles by provider
            List<ProviderVehicleGroup> vehiclesGroupedByProvider = groupVehiclesByProvider(allVehicles);

            // Get all providers for the dropdown
            List<Provider> providers = providerRepository.findAll();

            // Add enriched vehicle data for table view
            List<Map<String, Object>> enrichedVehicles = enrichVehicleData(allVehicles);

            // Add to model
            model.addAttribute("vehiclesGroupedByProvider", vehiclesGroupedByProvider);
            model.addAttribute("allVehicles", enrichedVehicles);
            model.addAttribute("providers", providers);

            // Get statistics
            model.addAttribute("totalVehicles", allVehicles.size());
            model.addAttribute("availableVehicles", countByStatus(allVehicles, "Available"));
            model.addAttribute("bookedVehicles", countByStatus(allVehicles, "Booked"));
            model.addAttribute("maintenanceVehicles", countByStatus(allVehicles, "Maintenance"));

            // Vehicle type breakdown
            Map<String, Long> vehicleTypeBreakdown = allVehicles.stream()
                    .collect(Collectors.groupingBy(
                            v -> v.getType() != null ? v.getType() : "Unknown",
                            Collectors.counting()
                    ));
            model.addAttribute("vehicleTypeBreakdown", vehicleTypeBreakdown);

        } catch (Exception e) {
            System.err.println("Error fetching vehicles: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("vehiclesGroupedByProvider", new ArrayList<>());
            model.addAttribute("allVehicles", new ArrayList<>());
            model.addAttribute("providers", new ArrayList<>());
            model.addAttribute("totalVehicles", 0);
            model.addAttribute("availableVehicles", 0);
            model.addAttribute("bookedVehicles", 0);
            model.addAttribute("maintenanceVehicles", 0);
            model.addAttribute("vehicleTypeBreakdown", new HashMap<>());
        }

        return "admin/vehicles";
    }

    /**
     * View single vehicle details
     */
    @GetMapping("/vehicles/{id}")
    public String viewVehicle(
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
            Transport vehicle = transportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            // Get provider information
            if (vehicle.getProviderId() != null) {
                try {
                    Provider provider = providerRepository.findById(vehicle.getProviderId()).orElse(null);
                    model.addAttribute("provider", provider);
                } catch (Exception e) {
                    System.err.println("Error fetching provider: " + e.getMessage());
                }
            }

            model.addAttribute("vehicle", vehicle);
            return "admin/vehicle-detail";
        } catch (Exception e) {
            System.err.println("Error fetching vehicle: " + e.getMessage());
            return "redirect:/admin/vehicles?error=vehicle_not_found";
        }
    }

    /**
     * Show edit vehicle form
     */
    @GetMapping("/vehicles/{id}/edit")
    public String editVehicleForm(
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
            Transport vehicle = transportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            // Get all providers for the dropdown
            List<Provider> providers = providerRepository.findAll();

            model.addAttribute("vehicle", vehicle);
            model.addAttribute("providers", providers);
            return "admin/vehicle-edit";
        } catch (Exception e) {
            System.err.println("Error loading vehicle for edit: " + e.getMessage());
            return "redirect:/admin/vehicles?error=vehicle_not_found";
        }
    }

    /**
     * Update vehicle via form submission
     */
    @PostMapping("/vehicles/{id}/update-form")
    public String updateVehicleForm(
            @PathVariable Long id,
            HttpSession session,
            @RequestParam Map<String, String> params
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            Transport vehicle = transportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            // Update basic information
            if (params.containsKey("type")) vehicle.setType(params.get("type"));
            if (params.containsKey("brand")) vehicle.setBrand(params.get("brand"));
            if (params.containsKey("model")) vehicle.setModel(params.get("model"));
            if (params.containsKey("color")) vehicle.setColor(params.get("color"));
            if (params.containsKey("plateNumber")) vehicle.setPlateNumber(params.get("plateNumber"));
            if (params.containsKey("capacity")) {
                try {
                    vehicle.setCapacity(Integer.parseInt(params.get("capacity")));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid capacity value");
                }
            }
            if (params.containsKey("description")) vehicle.setDescription(params.get("description"));
            if (params.containsKey("state")) vehicle.setState(params.get("state"));
            if (params.containsKey("company")) vehicle.setCompany(params.get("company"));
            if (params.containsKey("status")) vehicle.setStatus(params.get("status"));

            transportRepository.save(vehicle);

            return "redirect:/admin/vehicles/" + id + "?success=updated";
        } catch (Exception e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
            return "redirect:/admin/vehicles/" + id + "/edit?error=update_failed";
        }
    }

    /**
     * Get vehicle details as JSON (for AJAX)
     */
    @GetMapping("/vehicles/{id}/details")
    @ResponseBody
    public ResponseEntity<Transport> getVehicleDetails(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        try {
            Transport vehicle = transportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            return ResponseEntity.ok(vehicle);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create new vehicle (JSON API)
     */
    @PostMapping("/vehicles/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createVehicle(
            @RequestBody Transport vehicle,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            // Set defaults
            if (vehicle.getStatus() == null || vehicle.getStatus().isEmpty()) {
                vehicle.setStatus("Available");
            }

            Transport savedVehicle = transportRepository.save(vehicle);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Vehicle created successfully",
                    "vehicleId", savedVehicle.getId()
            ));
        } catch (Exception e) {
            System.err.println("Error creating vehicle: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error creating vehicle: " + e.getMessage()));
        }
    }

    /**
     * Update vehicle (JSON API)
     */
    @PostMapping("/vehicles/{id}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVehicle(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            Transport vehicle = transportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            updateVehicleFromMap(vehicle, data);
            transportRepository.save(vehicle);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Vehicle updated successfully"
            ));
        } catch (Exception e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error updating vehicle: " + e.getMessage()));
        }
    }

    /**
     * Delete vehicle
     */
    @PostMapping("/vehicles/{id}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteVehicle(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            if (!transportRepository.existsById(id)) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Vehicle not found"));
            }

            transportRepository.deleteById(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Vehicle deleted successfully"
            ));
        } catch (Exception e) {
            System.err.println("Error deleting vehicle: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error deleting vehicle: " + e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Group vehicles by provider
     */
    private List<ProviderVehicleGroup> groupVehiclesByProvider(List<Transport> vehicles) {
        // Group by provider ID
        Map<Long, ProviderVehicleGroup> groupMap = new LinkedHashMap<>();

        for (Transport vehicle : vehicles) {
            Long providerId = vehicle.getProviderId() != null ? vehicle.getProviderId() : 0L;

            ProviderVehicleGroup group = groupMap.get(providerId);
            if (group == null) {
                group = new ProviderVehicleGroup();
                group.setProviderId(providerId);

                if (providerId == 0L) {
                    // Unassigned vehicles
                    group.setProviderName("Unassigned Vehicles");
                    group.setProviderLogoUrl(null);
                    group.setProviderCity("N/A");
                    group.setProviderPhone("N/A");
                    group.setProviderRating(0.0);
                } else {
                    // Try to fetch provider info
                    try {
                        Provider provider = providerRepository.findById(providerId).orElse(null);

                        if (provider != null) {
                            group.setProviderName(provider.getCompanyName() != null ? provider.getCompanyName() :
                                    (provider.getFirstName() + " " + provider.getLastName()));
                            group.setProviderLogoUrl(provider.getCompanyLogoUrl());
                            group.setProviderCity(provider.getCity());
                            group.setProviderPhone(provider.getPhone());
                            group.setProviderRating(provider.getRating());
                        } else {
                            group.setProviderName("Unknown Provider (ID: " + providerId + ")");
                            group.setProviderLogoUrl(null);
                            group.setProviderCity("N/A");
                            group.setProviderPhone("N/A");
                            group.setProviderRating(0.0);
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching provider " + providerId + ": " + e.getMessage());
                        group.setProviderName("Error Loading Provider");
                        group.setProviderLogoUrl(null);
                        group.setProviderCity("N/A");
                        group.setProviderPhone("N/A");
                        group.setProviderRating(0.0);
                    }
                }

                group.setVehicles(new ArrayList<>());
                groupMap.put(providerId, group);
            }

            group.getVehicles().add(vehicle);
        }

        // Calculate statistics for each group
        for (ProviderVehicleGroup group : groupMap.values()) {
            List<Transport> groupVehicles = group.getVehicles();
            group.setTotalVehicles(groupVehicles.size());
            group.setAvailableVehicles(countByStatus(groupVehicles, "Available"));
            group.setBookedVehicles(countByStatus(groupVehicles, "Booked"));
            group.setMaintenanceVehicles(countByStatus(groupVehicles, "Maintenance"));
        }

        // Convert to list and sort by provider name
        List<ProviderVehicleGroup> result = groupMap.values().stream()
                .sorted(Comparator.comparing(ProviderVehicleGroup::getProviderName))
                .collect(Collectors.toList());

        // Move unassigned vehicles to the end if they exist
        ProviderVehicleGroup unassignedGroup = result.stream()
                .filter(g -> g.getProviderId() == 0L)
                .findFirst()
                .orElse(null);

        if (unassignedGroup != null) {
            result.remove(unassignedGroup);
            result.add(unassignedGroup);
        }

        return result;
    }

    /**
     * Enrich vehicle data with provider information
     */
    private List<Map<String, Object>> enrichVehicleData(List<Transport> vehicles) {
        List<Map<String, Object>> enrichedVehicles = new ArrayList<>();

        for (Transport vehicle : vehicles) {
            Map<String, Object> vehicleMap = new HashMap<>();
            vehicleMap.put("id", vehicle.getId());
            vehicleMap.put("type", vehicle.getType());
            vehicleMap.put("brand", vehicle.getBrand());
            vehicleMap.put("model", vehicle.getModel());
            vehicleMap.put("color", vehicle.getColor());
            vehicleMap.put("plateNumber", vehicle.getPlateNumber());
            vehicleMap.put("capacity", vehicle.getCapacity());
            vehicleMap.put("status", vehicle.getStatus());
            vehicleMap.put("state", vehicle.getState());
            vehicleMap.put("company", vehicle.getCompany());
            vehicleMap.put("description", vehicle.getDescription());

            // Get provider name
            String providerName = "N/A";
            if (vehicle.getProviderId() != null) {
                try {
                    Provider provider = providerRepository.findById(vehicle.getProviderId()).orElse(null);
                    if (provider != null) {
                        providerName = provider.getCompanyName() != null ? provider.getCompanyName() :
                                (provider.getFirstName() + " " + provider.getLastName());
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching provider for vehicle " + vehicle.getId());
                }
            }
            vehicleMap.put("providerName", providerName);

            enrichedVehicles.add(vehicleMap);
        }

        return enrichedVehicles;
    }

    /**
     * Update vehicle entity from map data
     */
    private void updateVehicleFromMap(Transport vehicle, Map<String, Object> data) {
        if (data.containsKey("type")) vehicle.setType((String) data.get("type"));
        if (data.containsKey("brand")) vehicle.setBrand((String) data.get("brand"));
        if (data.containsKey("model")) vehicle.setModel((String) data.get("model"));
        if (data.containsKey("color")) vehicle.setColor((String) data.get("color"));
        if (data.containsKey("plateNumber")) vehicle.setPlateNumber((String) data.get("plateNumber"));
        if (data.containsKey("capacity")) {
            Object capacityObj = data.get("capacity");
            if (capacityObj instanceof Integer) {
                vehicle.setCapacity((Integer) capacityObj);
            } else if (capacityObj instanceof String) {
                try {
                    vehicle.setCapacity(Integer.parseInt((String) capacityObj));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid capacity value");
                }
            }
        }
        if (data.containsKey("description")) vehicle.setDescription((String) data.get("description"));
        if (data.containsKey("state")) vehicle.setState((String) data.get("state"));
        if (data.containsKey("company")) vehicle.setCompany((String) data.get("company"));
        if (data.containsKey("status")) vehicle.setStatus((String) data.get("status"));
        if (data.containsKey("providerId")) {
            Object providerIdObj = data.get("providerId");
            if (providerIdObj instanceof Long) {
                vehicle.setProviderId((Long) providerIdObj);
            } else if (providerIdObj instanceof String) {
                try {
                    vehicle.setProviderId(Long.parseLong((String) providerIdObj));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid providerId value");
                }
            }
        }
    }

    /**
     * Count vehicles by status
     */
    private long countByStatus(List<Transport> vehicles, String status) {
        return vehicles.stream()
                .filter(vehicle -> status.equalsIgnoreCase(vehicle.getStatus()))
                .count();
    }

    // ==================== INNER CLASS ====================

    /**
     * Helper class for grouping vehicles by provider
     */
    @Setter
    @Getter
    public static class ProviderVehicleGroup {
        private Long providerId;
        private String providerName;
        private String providerLogoUrl;
        private String providerCity;
        private String providerPhone;
        private Double providerRating;
        private List<Transport> vehicles;
        private long totalVehicles;
        private long availableVehicles;
        private long bookedVehicles;
        private long maintenanceVehicles;
    }
}
