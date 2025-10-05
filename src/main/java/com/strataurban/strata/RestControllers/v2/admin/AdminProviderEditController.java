package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Enums.LogisticsServiceType;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Services.v2.ProviderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/providers")
public class AdminProviderEditController {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ProviderService providerService;

    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Show provider edit form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
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
            Provider provider = providerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Provider not found"));

            model.addAttribute("provider", provider);
            model.addAttribute("isEdit", true);

            // Add available service types for dropdown
            model.addAttribute("allServiceTypes", LogisticsServiceType.values());

            return "admin/provider-edit";
        } catch (Exception e) {
            System.err.println("Error fetching provider for edit: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Provider not found");
            return "redirect:/admin/providers";
        }
    }

    /**
     * Update provider
     */
    @PostMapping("/{id}/update")
    public String updateProvider(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String phone2,
            @RequestParam String companyName,
            @RequestParam(required = false) String companyAddress,
            @RequestParam(required = false) String companyRegistrationNumber,
            @RequestParam(required = false) String companyBusinessType,
            @RequestParam(required = false) String companyBusinessPhone,
            @RequestParam(required = false) String companyBusinessEmail,
            @RequestParam(required = false) String companyBusinessWebsite,
            @RequestParam(required = false) String description,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String country,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String primaryContactPosition,
            @RequestParam(required = false) String primaryContactDepartment,
            @RequestParam(required = false) String preferredLanguage,
            @RequestParam(required = false) List<String> serviceTypes,
            @RequestParam(required = false) Boolean emailVerified,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            // Validate required fields
            if (firstName == null || firstName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "First name is required");
                return "redirect:/admin/providers/" + id + "/edit?error=validation";
            }

            if (lastName == null || lastName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Last name is required");
                return "redirect:/admin/providers/" + id + "/edit?error=validation";
            }

            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                redirectAttributes.addFlashAttribute("error", "Valid email is required");
                return "redirect:/admin/providers/" + id + "/edit?error=validation";
            }

            if (companyName == null || companyName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Company name is required");
                return "redirect:/admin/providers/" + id + "/edit?error=validation";
            }

            Provider provider = providerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Provider not found"));

            // Check if email is already taken by another provider
            Provider existingProvider = providerRepository.findAll().stream()
                    .filter(p -> p.getEmail().equalsIgnoreCase(email.trim()) && !p.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (existingProvider != null) {
                redirectAttributes.addFlashAttribute("error", "Email already in use by another provider");
                return "redirect:/admin/providers/" + id + "/edit?error=duplicate_email";
            }

            // Update provider fields
            provider.setFirstName(firstName.trim());
            provider.setLastName(lastName.trim());
            provider.setEmail(email.trim());
            provider.setCompanyName(companyName.trim());
            provider.setCity(city.trim());
            provider.setState(state.trim());
            provider.setCountry(country.trim());

            // Optional fields
            if (phone != null && !phone.trim().isEmpty()) {
                provider.setPhone(phone.trim());
            }

            if (phone2 != null && !phone2.trim().isEmpty()) {
                provider.setPhone2(phone2.trim());
            }

            if (companyAddress != null && !companyAddress.trim().isEmpty()) {
                provider.setCompanyAddress(companyAddress.trim());
            }

            if (companyRegistrationNumber != null && !companyRegistrationNumber.trim().isEmpty()) {
                provider.setCompanyRegistrationNumber(companyRegistrationNumber.trim());
            }

            if (companyBusinessType != null && !companyBusinessType.trim().isEmpty()) {
                provider.setCompanyBusinessType(companyBusinessType.trim());
            }

            if (companyBusinessPhone != null && !companyBusinessPhone.trim().isEmpty()) {
                provider.setCompanyBusinessPhone(companyBusinessPhone.trim());
            }

            if (companyBusinessEmail != null && !companyBusinessEmail.trim().isEmpty()) {
                provider.setCompanyBusinessEmail(companyBusinessEmail.trim());
            }

            if (companyBusinessWebsite != null && !companyBusinessWebsite.trim().isEmpty()) {
                provider.setCompanyBusinessWebsite(companyBusinessWebsite.trim());
            }

            if (description != null && !description.trim().isEmpty()) {
                provider.setDescription(description.trim());
            }

            if (zipCode != null && !zipCode.trim().isEmpty()) {
                provider.setZipCode(zipCode.trim());
            }

            if (address != null && !address.trim().isEmpty()) {
                provider.setAddress(address.trim());
            }

            if (primaryContactPosition != null && !primaryContactPosition.trim().isEmpty()) {
                provider.setPrimaryContactPosition(primaryContactPosition.trim());
            }

            if (primaryContactDepartment != null && !primaryContactDepartment.trim().isEmpty()) {
                provider.setPrimaryContactDepartment(primaryContactDepartment.trim());
            }

            if (preferredLanguage != null && !preferredLanguage.trim().isEmpty()) {
                provider.setPreferredLanguage(preferredLanguage.trim());
            }

            // Handle service types
            if (serviceTypes != null && !serviceTypes.isEmpty()) {
                List<LogisticsServiceType> enumServiceTypes = new ArrayList<>();
                for (String type : serviceTypes) {
                    try {
                        enumServiceTypes.add(LogisticsServiceType.valueOf(type));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid service type: " + type);
                    }
                }
                provider.setServiceTypes(enumServiceTypes);
            }

            // Handle email verification
            if (emailVerified != null) {
                provider.setEmailVerified(emailVerified);
            }

            providerRepository.save(provider);
            redirectAttributes.addFlashAttribute("success", "Provider updated successfully");
            return "redirect:/admin/providers/" + id + "?success=updated";

        } catch (Exception e) {
            System.err.println("Error updating provider: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update provider: " + e.getMessage());
            return "redirect:/admin/providers/" + id + "/edit?error=update_failed";
        }
    }

    /**
     * Validate provider data (AJAX)
     */
    @PostMapping("/{id}/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateProviderData(
            @PathVariable Long id,
            @RequestBody Map<String, String> data,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Unauthorized"));
        }

        try {
            String field = data.get("field");
            String value = data.get("value");

            if ("email".equals(field)) {
                Provider existingProvider = providerRepository.findAll().stream()
                        .filter(p -> p.getEmail().equalsIgnoreCase(value) && !p.getId().equals(id))
                        .findFirst()
                        .orElse(null);

                if (existingProvider != null) {
                    return ResponseEntity.ok(Map.of(
                            "valid", false,
                            "message", "Email already in use"
                    ));
                }
            }

            return ResponseEntity.ok(Map.of("valid", true));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("valid", false, "message", "Validation error"));
        }
    }

    /**
     * Upload provider logo (AJAX)
     */
    @PostMapping("/{id}/upload-logo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadLogo(
            @PathVariable Long id,
            @RequestParam String logoUrl,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            Provider provider = providerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Provider not found"));

            provider.setCompanyLogoUrl(logoUrl);
            providerRepository.save(provider);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Logo updated successfully",
                    "logoUrl", logoUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to update logo"));
        }
    }

    /**
     * Get provider edit data (AJAX)
     */
    @GetMapping("/{id}/edit-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProviderEditData(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        try {
            Provider provider = providerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Provider not found"));

            return ResponseEntity.ok(Map.of(
                    "id", provider.getId(),
                    "firstName", provider.getFirstName(),
                    "lastName", provider.getLastName(),
                    "email", provider.getEmail(),
                    "companyName", provider.getCompanyName(),
                    "city", provider.getCity(),
                    "state", provider.getState(),
                    "country", provider.getCountry(),
                    "rating", provider.getRating() != null ? provider.getRating() : 0.0,
                    "numberOfRatings", provider.getNumberOfRatings()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch provider data"));
        }
    }
}