package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ServiceArea;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.ServiceAreaRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.Services.v2.ServiceAreaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
public class AdminServiceAreaAssignController {

    @Autowired
    private ServiceAreaRepository serviceAreaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceAreaService serviceAreaService;

    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Show provider assignment page
     */
    @GetMapping("/{id}/assign-providers")
    public String showAssignProvidersPage(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Boolean emailVerified,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            ServiceArea serviceArea = serviceAreaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Service area not found"));

            // Get all providers with filtering
            Specification<User> spec = Specification.where(
                    (root, query, cb) -> cb.equal(root.get("roles"), EnumRoles.PROVIDER)
            );

            // Apply search filter
            if (search != null && !search.trim().isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("firstName")), "%" + search.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("lastName")), "%" + search.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("companyName")), "%" + search.toLowerCase() + "%")
                        )
                );
            }

            // Apply country filter
            if (country != null && !country.trim().isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%")
                );
            }

            // Apply state filter
            if (state != null && !state.trim().isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("state")), "%" + state.toLowerCase() + "%")
                );
            }

            // Apply city filter
            if (city != null && !city.trim().isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%")
                );
            }

            // Apply email verification filter
            if (emailVerified != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("emailVerified"), emailVerified)
                );
            }

            // Apply sorting
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            List<User> allUsers = userRepository.findAll(spec, sort);
            List<Provider> allProviders = allUsers.stream()
                    .filter(user -> user instanceof Provider)
                    .map(user -> (Provider) user)
                    .collect(Collectors.toList());

            // Get providers already assigned to this area
            List<Provider> assignedProviders = serviceAreaService.getProvidersInServiceArea(id);
            Set<Long> assignedProviderIds = assignedProviders.stream()
                    .map(Provider::getId)
                    .collect(Collectors.toSet());

            // Separate assigned and unassigned providers
            List<Provider> unassignedProviders = allProviders.stream()
                    .filter(p -> !assignedProviderIds.contains(p.getId()))
                    .collect(Collectors.toList());

            model.addAttribute("serviceArea", serviceArea);
            model.addAttribute("allProviders", allProviders);
            model.addAttribute("assignedProviders", assignedProviders);
            model.addAttribute("unassignedProviders", unassignedProviders);
            model.addAttribute("assignedProviderIds", assignedProviderIds);

            // Filter parameters for form
            model.addAttribute("search", search);
            model.addAttribute("country", country);
            model.addAttribute("state", state);
            model.addAttribute("city", city);
            model.addAttribute("serviceType", serviceType);
            model.addAttribute("emailVerified", emailVerified);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);

            // Statistics
            model.addAttribute("totalProviders", allProviders.size());
            model.addAttribute("assignedCount", assignedProviders.size());
            model.addAttribute("unassignedCount", unassignedProviders.size());

        } catch (Exception e) {
            System.err.println("Error loading assign providers page: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to load providers");
            return "redirect:/admin/areas/" + id;
        }

        return "admin/assign-providers";
    }

    /**
     * Assign providers to service area
     */
    @PostMapping("/{id}/assign-providers")
    public String assignProviders(
            @PathVariable Long id,
            @RequestParam(value = "providerIds", required = false) List<Long> providerIds,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            if (providerIds == null || providerIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No providers selected");
                return "redirect:/admin/areas/" + id + "/assign-providers";
            }

            serviceAreaService.addServiceAreasToProvider(id, providerIds);
            redirectAttributes.addFlashAttribute("success",
                    providerIds.size() + " provider(s) assigned successfully");

            return "redirect:/admin/areas/" + id + "?success=assigned";

        } catch (Exception e) {
            System.err.println("Error assigning providers: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to assign providers: " + e.getMessage());
            return "redirect:/admin/areas/" + id + "/assign-providers";
        }
    }

    /**
     * Unassign provider from service area (AJAX)
     */
    @PostMapping("/{areaId}/unassign/{providerId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unassignProvider(
            @PathVariable Long areaId,
            @PathVariable Long providerId,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            serviceAreaService.removeServiceAreasFromProvider(providerId, List.of(areaId));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Provider unassigned successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to unassign provider"));
        }
    }

    /**
     * Bulk assign providers (AJAX)
     */
    @PostMapping("/{id}/bulk-assign")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkAssignProviders(
            @PathVariable Long id,
            @RequestBody Map<String, List<Integer>> request,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            List<Integer> providerIdsInt = request.get("providerIds");
            if (providerIdsInt == null || providerIdsInt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "No providers selected"));
            }

            List<Long> providerIds = providerIdsInt.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());

            serviceAreaService.addServiceAreasToProvider(id, providerIds);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", providerIds.size() + " provider(s) assigned successfully",
                    "count", providerIds.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to assign providers"));
        }
    }

    /**
     * Get provider assignment status (AJAX)
     */
    @GetMapping("/{areaId}/provider-status/{providerId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProviderStatus(
            @PathVariable Long areaId,
            @PathVariable Long providerId,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        try {
            List<Provider> assignedProviders = serviceAreaService.getProvidersInServiceArea(areaId);
            boolean isAssigned = assignedProviders.stream()
                    .anyMatch(p -> p.getId().equals(providerId));

            return ResponseEntity.ok(Map.of(
                    "isAssigned", isAssigned,
                    "providerId", providerId,
                    "areaId", areaId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch status"));
        }
    }
}