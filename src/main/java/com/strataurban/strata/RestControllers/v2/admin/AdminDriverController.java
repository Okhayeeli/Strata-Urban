package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.Passengers.Driver;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.DriverRepository;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.ServiceImpls.v2.BookingServiceImpl;
import com.strataurban.strata.ServiceImpls.v2.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminDriverController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BookingServiceImpl bookingServiceImpl;
    @Autowired
    private DriverRepository driverRepository;

    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Display drivers management page with provider grouping
     */
    @GetMapping("/drivers")
    public String showDrivers(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean verified,
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
            // Get all drivers (users with DRIVER role)
            List<Driver> allDrivers = driverRepository.findAll();

            // Build specification for filtering if needed
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                allDrivers = allDrivers.stream()
                        .filter(driver ->
                                (driver.getFirstName() != null && driver.getFirstName().toLowerCase().contains(searchLower)) ||
                                        (driver.getLastName() != null && driver.getLastName().toLowerCase().contains(searchLower)) ||
                                        (driver.getEmail() != null && driver.getEmail().toLowerCase().contains(searchLower)) ||
                                        (driver.getPhone() != null && driver.getPhone().toLowerCase().contains(searchLower))
                        )
                        .collect(Collectors.toList());
            }

            if (verified != null) {
                allDrivers = allDrivers.stream()
                        .filter(driver -> driver.isEmailVerified() == verified)
                        .collect(Collectors.toList());
            }

            if (providerId != null) {
                allDrivers = allDrivers.stream()
                        .filter(driver -> driver.getProviderId() != null &&
                                driver.getProviderId().equals(String.valueOf(providerId)))
                        .collect(Collectors.toList());
            }

            // Group drivers by provider
            List<ProviderDriverGroup> driversGroupedByProvider = groupDriversByProvider(allDrivers);

            // Get all providers for the dropdown
            List<Provider> providers = providerRepository.findAll();

            // Add enriched driver data for table view
            List<Map<String, Object>> enrichedDrivers = enrichDriverData(allDrivers);

            // Add to model
            model.addAttribute("driversGroupedByProvider", driversGroupedByProvider);
            model.addAttribute("allDrivers", enrichedDrivers);
            model.addAttribute("providers", providers);

            // Get statistics
            model.addAttribute("totalDrivers", allDrivers.size());
            model.addAttribute("activeDrivers", countActiveDrivers(allDrivers));
            model.addAttribute("availableDrivers", countAvailableDrivers(allDrivers)); // Can be refined based on actual availability logic
            model.addAttribute("verifiedDrivers", countVerifiedDrivers(allDrivers));

        } catch (Exception e) {
            System.err.println("Error fetching drivers: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("driversGroupedByProvider", new ArrayList<>());
            model.addAttribute("allDrivers", new ArrayList<>());
            model.addAttribute("providers", new ArrayList<>());
            model.addAttribute("totalDrivers", 0);
            model.addAttribute("activeDrivers", 0);
            model.addAttribute("availableDrivers", 0);
            model.addAttribute("verifiedDrivers", 0);
        }

        return "admin/drivers";
    }

    /**
     * View single driver details
     */
    @GetMapping("/drivers/{id}")
    public String viewDriver(
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
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            // Get provider information
            if (driver.getProviderId() != null && !driver.getProviderId().isEmpty()) {
                try {
                    Long providerIdLong = Long.parseLong(driver.getProviderId());
                    Provider provider = providerRepository.findById(providerIdLong).orElse(null);
                    model.addAttribute("provider", provider);
                } catch (Exception e) {
                    System.err.println("Error fetching provider: " + e.getMessage());
                }
            }

            model.addAttribute("driver", driver);
            return "admin/driver-detail";
        } catch (Exception e) {
            System.err.println("Error fetching driver: " + e.getMessage());
            return "redirect:/admin/drivers?error=driver_not_found";
        }
    }

    /**
     * Show edit driver form
     */
    @GetMapping("/drivers/{id}/edit")
    public String editDriverForm(
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
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            // Get all providers for dropdown
            List<Provider> providers = providerRepository.findAll();

            model.addAttribute("driver", driver);
            model.addAttribute("providers", providers);
            return "admin/driver-edit";
        } catch (Exception e) {
            System.err.println("Error fetching driver for edit: " + e.getMessage());
            return "redirect:/admin/drivers?error=driver_not_found";
        }
    }

    /**
     * Update driver via form submission
     */
    @PostMapping("/drivers/{id}/update-form")
    public String updateDriverForm(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String middleName,
            @RequestParam(required = false) String title,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String phone2,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String preferredLanguage,
            @RequestParam(required = false) Boolean emailVerified,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            driver.setFirstName(firstName);
            driver.setLastName(lastName);
            driver.setMiddleName(middleName);
            driver.setTitle(title);
            driver.setEmail(email);
            driver.setPhone(phone);
            driver.setPhone2(phone2);
            driver.setCity(city);
            driver.setState(state);
            driver.setCountry(country);
            driver.setAddress(address);
            driver.setPreferredLanguage(preferredLanguage);

            if (emailVerified != null) {
                driver.setEmailVerified(emailVerified);
            }

            userRepository.save(driver);
            return "redirect:/admin/drivers/" + id + "?success=updated";
        } catch (Exception e) {
            System.err.println("Error updating driver: " + e.getMessage());
            return "redirect:/admin/drivers/" + id + "/edit?error=update_failed";
        }
    }

    /**
     * Get driver details as JSON (for edit modal)
     */
    @GetMapping("/drivers/{id}/details")
    @ResponseBody
    public ResponseEntity<?> getDriverDetails(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            Map<String, Object> driverData = new HashMap<>();
            driverData.put("id", driver.getId());
            driverData.put("firstName", driver.getFirstName());
            driverData.put("lastName", driver.getLastName());
            driverData.put("middleName", driver.getMiddleName());
            driverData.put("email", driver.getEmail());
            driverData.put("phone", driver.getPhone());
            driverData.put("phone2", driver.getPhone2());
            driverData.put("providerId", driver.getProviderId());
            driverData.put("city", driver.getCity());
            driverData.put("state", driver.getState());
            driverData.put("country", driver.getCountry());
            driverData.put("address", driver.getAddress());
            driverData.put("emailVerified", driver.isEmailVerified());
            driverData.put("imageUrl", driver.getImageUrl());

            return ResponseEntity.ok(driverData);
        } catch (Exception e) {
            System.err.println("Error fetching driver details: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch driver details"));
        }
    }

    /**
     * Create new driver
     */
    @PostMapping("/drivers/create")
    @ResponseBody
    public ResponseEntity<?> createDriver(
            @RequestBody Map<String, Object> driverData,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            // Validate required fields
            String email = (String) driverData.get("email");
            String username = (String) driverData.get("username");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            // Generate username from email if not provided
            if (username == null || username.isEmpty()) {
                username = email.split("@")[0];
            }

            // Check if user already exists
            if (userRepository.findByUsernameOrEmail(username, email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username or email already exists"));
            }

            // Create new driver
            User driver = new User();
            updateDriverFromMap(driver, driverData);
            driver.setRoles(EnumRoles.DRIVER);
            driver.setUsername(username);
            driver.setSelfCreated(false);

            // Set default password if not provided
            String password = (String) driverData.get("password");
            if (password == null || password.isEmpty()) {
                password = "Driver@123"; // Default password
            }
            driver.setPassword(passwordEncoder.encode(password));

            userRepository.save(driver);

            return ResponseEntity.ok(Map.of(
                    "message", "Driver created successfully",
                    "driverId", driver.getId()
            ));
        } catch (Exception e) {
            System.err.println("Error creating driver: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create driver: " + e.getMessage()));
        }
    }

    /**
     * Update driver
     */
    @PostMapping("/drivers/{id}/update")
    @ResponseBody
    public ResponseEntity<?> updateDriver(
            @PathVariable Long id,
            @RequestBody Map<String, Object> driverData,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            updateDriverFromMap(driver, driverData);
            userRepository.save(driver);

            return ResponseEntity.ok(Map.of("message", "Driver updated successfully"));
        } catch (Exception e) {
            System.err.println("Error updating driver: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update driver: " + e.getMessage()));
        }
    }

    /**
     * Delete driver
     */
    @PostMapping("/drivers/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteDriver(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Driver deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting driver: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete driver"));
        }
    }

    /**
     * Verify driver email
     */
    @PostMapping("/drivers/{id}/verify")
    @ResponseBody
    public ResponseEntity<?> verifyDriver(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            driver.setEmailVerified(true);
            userRepository.save(driver);

            return ResponseEntity.ok(Map.of("message", "Driver verified successfully"));
        } catch (Exception e) {
            System.err.println("Error verifying driver: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to verify driver"));
        }
    }

    /**
     * Unverify driver email
     */
    @PostMapping("/drivers/{id}/unverify")
    @ResponseBody
    public ResponseEntity<?> unverifyDriver(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            driver.setEmailVerified(false);
            userRepository.save(driver);

            return ResponseEntity.ok(Map.of("message", "Driver unverified successfully"));
        } catch (Exception e) {
            System.err.println("Error unverifying driver: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to unverify driver"));
        }
    }

    /**
     * Lock driver account
     */
    @PostMapping("/drivers/{id}/lock")
    @ResponseBody
    public ResponseEntity<?> lockDriver(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            driver.setAccountLockedUntil(LocalDateTime.now().plusYears(100));
            userRepository.save(driver);

            return ResponseEntity.ok(Map.of("message", "Driver account locked successfully"));
        } catch (Exception e) {
            System.err.println("Error locking driver: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to lock driver"));
        }
    }

    /**
     * Unlock driver account
     */
    @PostMapping("/drivers/{id}/unlock")
    @ResponseBody
    public ResponseEntity<?> unlockDriver(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            User driver = userRepository.findByIdAndRoles(id, EnumRoles.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            driver.setAccountLockedUntil(null);
            userRepository.save(driver);

            return ResponseEntity.ok(Map.of("message", "Driver account unlocked successfully"));
        } catch (Exception e) {
            System.err.println("Error unlocking driver: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to unlock driver"));
        }
    }

    /**
     * Get driver statistics as JSON
     */
    @GetMapping("/drivers/stats")
    @ResponseBody
    public ResponseEntity<?> getDriverStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            List<Driver> allDrivers = driverRepository.findAll();

            return ResponseEntity.ok(Map.of(
                    "totalDrivers", allDrivers.size(),
                    "activeDrivers", countActiveDrivers(allDrivers),
                    "availableDrivers", countActiveDrivers(allDrivers),
                    "verifiedDrivers", countVerifiedDrivers(allDrivers),
                    "totalProviders", providerRepository.count()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch statistics"));
        }
    }

    /**
     * Get drivers by provider
     */
    @GetMapping("/drivers/by-provider/{providerId}")
    @ResponseBody
    public ResponseEntity<?> getDriversByProvider(
            @PathVariable Long providerId,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            List<User> drivers = userRepository.findByRolesAndProviderId(
                    EnumRoles.DRIVER,
                    String.valueOf(providerId)
            );

            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch drivers"));
        }
    }

    /**
     * Get driver count for a provider
     */
    @GetMapping("/drivers/count-by-provider/{providerId}")
    @ResponseBody
    public ResponseEntity<?> getDriverCountByProvider(
            @PathVariable Long providerId,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            Integer count = userRepository.countByRolesAndProviderId(
                    EnumRoles.DRIVER,
                    String.valueOf(providerId)
            );

            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch driver count"));
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Group drivers by their providers
     */
    private List<ProviderDriverGroup> groupDriversByProvider(List<Driver> drivers) {
        Map<Long, ProviderDriverGroup> groupMap = new HashMap<>();

        for (Driver driver : drivers) {
            Long providerId = null;
            try {
                if (driver.getProviderId() != null && !driver.getProviderId().isEmpty()) {
                    providerId = Long.parseLong(driver.getProviderId());
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid provider ID for driver " + driver.getId() + ": " + driver.getProviderId());
                continue;
            }

            if (providerId == null) {
                // Create a group for drivers without provider
                providerId = 0L; // Using 0 for unassigned drivers
            }

            ProviderDriverGroup group = groupMap.get(providerId);

            if (group == null) {
                // Create new group
                group = new ProviderDriverGroup();
                group.setProviderId(providerId);

                if (providerId == 0L) {
                    // Unassigned drivers
                    group.setProviderName("Unassigned Drivers");
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

                group.setDrivers(new ArrayList<>());
                groupMap.put(providerId, group);
            }

            group.getDrivers().add(driver);
        }

        // Calculate statistics for each group
        for (ProviderDriverGroup group : groupMap.values()) {
            group.setTotalDrivers(group.getDrivers().size());
            group.setActiveDrivers(countActiveDrivers(group.getDrivers()));
            group.setAvailableDrivers(countAvailableDrivers(group.getDrivers()));
        }

        // Convert to list and sort by provider name
        List<ProviderDriverGroup> result = groupMap.values().stream()
                .sorted(Comparator.comparing(ProviderDriverGroup::getProviderName))
                .collect(Collectors.toList());

        // Move unassigned drivers to the end if they exist
        ProviderDriverGroup unassignedGroup = result.stream()
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
     * Enrich driver data with provider information
     */
    private List<Map<String, Object>> enrichDriverData(List<Driver> drivers) {
        List<Map<String, Object>> enrichedDrivers = new ArrayList<>();

        for (User driver : drivers) {
            Map<String, Object> driverMap = new HashMap<>();
            driverMap.put("id", driver.getId());
            driverMap.put("firstName", driver.getFirstName());
            driverMap.put("lastName", driver.getLastName());
            driverMap.put("middleName", driver.getMiddleName());
            driverMap.put("email", driver.getEmail());
            driverMap.put("phone", driver.getPhone());
            driverMap.put("city", driver.getCity());
            driverMap.put("state", driver.getState());
            driverMap.put("emailVerified", driver.isEmailVerified());
            driverMap.put("imageUrl", driver.getImageUrl());

            // Check if account is locked
            boolean isActive = driver.getAccountLockedUntil() == null ||
                    driver.getAccountLockedUntil().isBefore(LocalDateTime.now());
            driverMap.put("isActive", isActive);

            // Get provider name
            String providerName = "N/A";
            if (driver.getProviderId() != null && !driver.getProviderId().isEmpty()) {
                try {
                    Long providerId = Long.parseLong(driver.getProviderId());
                    Provider provider = providerRepository.findById(providerId).orElse(null);
                    if (provider != null) {
                        providerName = provider.getCompanyName() != null ? provider.getCompanyName() :
                                (provider.getFirstName() + " " + provider.getLastName());
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching provider for driver " + driver.getId());
                }
            }
            driverMap.put("providerName", providerName);

            enrichedDrivers.add(driverMap);
        }

        return enrichedDrivers;
    }

    /**
     * Update driver entity from map data
     */
    private void updateDriverFromMap(User driver, Map<String, Object> data) {
        if (data.containsKey("firstName")) driver.setFirstName((String) data.get("firstName"));
        if (data.containsKey("lastName")) driver.setLastName((String) data.get("lastName"));
        if (data.containsKey("middleName")) driver.setMiddleName((String) data.get("middleName"));
        if (data.containsKey("email")) driver.setEmail((String) data.get("email"));
        if (data.containsKey("phone")) driver.setPhone((String) data.get("phone"));
        if (data.containsKey("phone2")) driver.setPhone2((String) data.get("phone2"));
        if (data.containsKey("providerId")) {
            Object providerIdObj = data.get("providerId");
            driver.setProviderId(providerIdObj != null ? String.valueOf(providerIdObj) : null);
        }
        if (data.containsKey("city")) driver.setCity((String) data.get("city"));
        if (data.containsKey("state")) driver.setState((String) data.get("state"));
        if (data.containsKey("country")) driver.setCountry((String) data.get("country"));
        if (data.containsKey("address")) driver.setAddress((String) data.get("address"));
        if (data.containsKey("emailVerified")) driver.setEmailVerified((Boolean) data.get("emailVerified"));
        if (data.containsKey("imageUrl")) driver.setImageUrl((String) data.get("imageUrl"));
    }

    /**
     * Count active drivers (not locked)
     */
    private long countActiveDrivers(List<Driver> drivers) {
        return drivers.stream()
                .filter(driver ->
                        driver.isActive()
                                && (driver.getAccountLockedUntil() == null
                                || driver.getAccountLockedUntil().isBefore(LocalDateTime.now()))
                )
                .count();
    }


    private long countAvailableDrivers(List<Driver> drivers) {
        return drivers.stream()
                .filter(driver -> {
                    boolean isActive = driver.getAccountLockedUntil() == null;
                    boolean isVerified = driver.isEmailVerified();
                    boolean isAvailable = driver.isAvailable();
                    return isActive && isVerified && isAvailable;
                })
                .count();
    }
    /**
     * Count verified drivers
     */
    private long countVerifiedDrivers(List<Driver> drivers) {
        return drivers.stream().filter(Driver::isEmailVerified).count();
    }

    // ==================== INNER CLASS ====================

    /**
     * Helper class for grouping drivers by provider
     */
    @Setter
    @Getter
    public static class ProviderDriverGroup {
        private Long providerId;
        private String providerName;
        private String providerLogoUrl;
        private String providerCity;
        private String providerPhone;
        private Double providerRating;
        private List<Driver> drivers;
        private long totalDrivers;
        private long activeDrivers;
        private long availableDrivers;

    }
}
