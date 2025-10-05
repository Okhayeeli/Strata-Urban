package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.DTOs.v2.ServiceAreaReportDTO;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ServiceArea;
import com.strataurban.strata.Repositories.v2.ServiceAreaRepository;
import com.strataurban.strata.Services.v2.ServiceAreaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/areas")
public class AdminServiceAreaController {

    @Autowired
    private ServiceAreaService serviceAreaService;

    @Autowired
    private ServiceAreaRepository serviceAreaRepository;

    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Display all service areas with filtering and sorting
     */
    @GetMapping({"", "/"})
    public String listServiceAreas(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {

        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            // Set default sorting
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "name";
            }
            if (sortDir == null || sortDir.isEmpty()) {
                sortDir = "asc";
            }

            // Get all service areas with sorting
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            List<ServiceArea> serviceAreas = serviceAreaRepository.findAll(sort);

            // Apply search filter if provided
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase().trim();
                serviceAreas = serviceAreas.stream()
                        .filter(area ->
                                area.getName().toLowerCase().contains(searchLower) ||
                                        (area.getDescription() != null && area.getDescription().toLowerCase().contains(searchLower))
                        )
                        .collect(Collectors.toList());
            }

            // Get service area report with provider counts
            List<ServiceAreaReportDTO> areaReport = serviceAreaService.getServiceAreaReport();
            Map<Long, Long> providerCounts = new HashMap<>();
            for (ServiceAreaReportDTO report : areaReport) {
                providerCounts.put(report.getServiceAreaId(), (long) report.getProviderCount());
            }

            // Add attributes to model
            model.addAttribute("serviceAreas", serviceAreas);
            model.addAttribute("providerCounts", providerCounts);
            model.addAttribute("totalAreas", serviceAreas.size());
            model.addAttribute("search", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("oppositeSortDir", sortDir.equals("asc") ? "desc" : "asc");

        } catch (Exception e) {
            model.addAttribute("error", "Error loading service areas: " + e.getMessage());
            model.addAttribute("serviceAreas", new ArrayList<>());
        }

        return "admin/service-areas";
    }

    /**
     * Show create service area form
     */
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);
        model.addAttribute("serviceArea", new ServiceArea());
        model.addAttribute("isEdit", false);

        return "admin/service-area-form";
    }

    /**
     * Create a new service area
     */
    @PostMapping("/create")
    public String createServiceArea(
            HttpSession session,
            @ModelAttribute ServiceArea serviceArea,
            RedirectAttributes redirectAttributes) {

        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            // Validate input
            if (serviceArea.getName() == null || serviceArea.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Service area name is required");
                return "redirect:/admin/areas/create";
            }

            // Check for duplicate name
            List<ServiceArea> existingAreas = serviceAreaRepository.findAll();
            boolean nameExists = existingAreas.stream()
                    .anyMatch(area -> area.getName().equalsIgnoreCase(serviceArea.getName().trim()));

            if (nameExists) {
                redirectAttributes.addFlashAttribute("error", "A service area with this name already exists");
                return "redirect:/admin/areas/create";
            }

            serviceArea.setName(serviceArea.getName().trim());
            if (serviceArea.getDescription() != null) {
                serviceArea.setDescription(serviceArea.getDescription().trim());
            }

            serviceAreaService.createServiceArea(serviceArea);
            redirectAttributes.addFlashAttribute("success", "Service area created successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating service area: " + e.getMessage());
            return "redirect:/admin/areas/create";
        }

        return "redirect:/admin/areas";
    }

    /**
     * Show edit service area form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(
            HttpSession session,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            ServiceArea serviceArea = serviceAreaService.getServiceAreaById(id);
            model.addAttribute("serviceArea", serviceArea);
            model.addAttribute("isEdit", true);

            // Get providers in this area
            List<Provider> providers = serviceAreaService.getProvidersInServiceArea(id);
            model.addAttribute("providersInArea", providers);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Service area not found");
            return "redirect:/admin/areas";
        }

        return "admin/service-area-form";
    }

    /**
     * Update a service area
     */
    @PostMapping("/edit/{id}")
    public String updateServiceArea(
            HttpSession session,
            @PathVariable Long id,
            @ModelAttribute ServiceArea serviceArea,
            RedirectAttributes redirectAttributes) {

        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            // Validate input
            if (serviceArea.getName() == null || serviceArea.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Service area name is required");
                return "redirect:/admin/areas/edit/" + id;
            }

            // Check for duplicate name (excluding current area)
            List<ServiceArea> existingAreas = serviceAreaRepository.findAll();
            boolean nameExists = existingAreas.stream()
                    .anyMatch(area -> !area.getId().equals(id) &&
                            area.getName().equalsIgnoreCase(serviceArea.getName().trim()));

            if (nameExists) {
                redirectAttributes.addFlashAttribute("error", "A service area with this name already exists");
                return "redirect:/admin/areas/edit/" + id;
            }

            serviceArea.setName(serviceArea.getName().trim());
            if (serviceArea.getDescription() != null) {
                serviceArea.setDescription(serviceArea.getDescription().trim());
            }

            serviceAreaService.updateServiceArea(id, serviceArea);
            redirectAttributes.addFlashAttribute("success", "Service area updated successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating service area: " + e.getMessage());
            return "redirect:/admin/areas/edit/" + id;
        }

        return "redirect:/admin/areas";
    }

    /**
     * Delete a service area
     */
    @PostMapping("/delete/{id}")
    public String deleteServiceArea(
            HttpSession session,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            // Check if any providers are using this area
            List<Provider> providers = serviceAreaService.getProvidersInServiceArea(id);
            if (!providers.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Cannot delete service area. " + providers.size() + " provider(s) are operating in this area.");
                return "redirect:/admin/areas";
            }

            serviceAreaService.deleteServiceArea(id);
            redirectAttributes.addFlashAttribute("success", "Service area deleted successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting service area: " + e.getMessage());
        }

        return "redirect:/admin/areas";
    }

    /**
     * View service area details
     */
    @GetMapping("/view/{id}")
    public String viewServiceArea(
            HttpSession session,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            ServiceArea serviceArea = serviceAreaService.getServiceAreaById(id);
            List<Provider> providers = serviceAreaService.getProvidersInServiceArea(id);

            model.addAttribute("serviceArea", serviceArea);
            model.addAttribute("providers", providers);
            model.addAttribute("providerCount", providers.size());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Service area not found");
            return "redirect:/admin/areas";
        }

        return "admin/service-area-details";
    }

    /**
     * Bulk delete service areas (AJAX)
     */
    @PostMapping("/bulk-delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkDeleteServiceAreas(
            HttpSession session,
            @RequestBody Map<String, List<Long>> request) {

        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "No service areas selected"));
            }

            int deletedCount = 0;
            int skippedCount = 0;
            List<String> skippedAreas = new ArrayList<>();

            for (Long id : ids) {
                try {
                    List<Provider> providers = serviceAreaService.getProvidersInServiceArea(id);
                    if (providers.isEmpty()) {
                        serviceAreaService.deleteServiceArea(id);
                        deletedCount++;
                    } else {
                        ServiceArea area = serviceAreaService.getServiceAreaById(id);
                        skippedAreas.add(area.getName());
                        skippedCount++;
                    }
                } catch (Exception e) {
                    skippedCount++;
                }
            }

            String message = deletedCount + " service area(s) deleted successfully";
            if (skippedCount > 0) {
                message += ". " + skippedCount + " area(s) skipped (has providers)";
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", message,
                    "deleted", deletedCount,
                    "skipped", skippedCount,
                    "skippedAreas", skippedAreas
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Export service areas report (AJAX)
     */
    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> exportServiceAreas(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<ServiceAreaReportDTO> report = serviceAreaService.getServiceAreaReport();
            List<Map<String, Object>> exportData = report.stream()
                    .map(dto -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", dto.getServiceAreaId());
                        map.put("name", dto.getServiceAreaName());
                        map.put("description", dto.getServiceAreaDescription());
                        map.put("providerCount", dto.getProviderCount());
                        map.put("providers", dto.getProviders());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(exportData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get service area statistics (AJAX)
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getServiceAreaStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        try {
            List<ServiceArea> allAreas = serviceAreaRepository.findAll();
            List<ServiceAreaReportDTO> report = serviceAreaService.getServiceAreaReport();

            long totalAreas = allAreas.size();
            long areasWithProviders = report.stream()
                    .filter(dto -> dto.getProviderCount() > 0)
                    .count();
            long areasWithoutProviders = totalAreas - areasWithProviders;
            long totalProviders = report.stream()
                    .mapToLong(ServiceAreaReportDTO::getProviderCount)
                    .sum();

            return ResponseEntity.ok(Map.of(
                    "totalAreas", totalAreas,
                    "areasWithProviders", areasWithProviders,
                    "areasWithoutProviders", areasWithoutProviders,
                    "totalProviders", totalProviders,
                    "averageProvidersPerArea", totalAreas > 0 ? (double) totalProviders / totalAreas : 0.0
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
