package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/providers")
public class AdminProviderController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProviderRepository providerRepository;

    private static final String API_BASE_URL = "http://localhost:8080/api/v2/providers";

    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Get access token from session
     */
    private String getAccessToken(HttpSession session) {
        return (String) session.getAttribute("accessToken");
    }

    /**
     * Show providers list page
     */
    @GetMapping({"", "/"})
    public String showProvidersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minRating,
            HttpSession session,
            Model model
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            // Fetch providers from database with pagination
            Page<Provider> providersPage = getProvidersPage(page, size, name, serviceType, city, minRating);
            model.addAttribute("providers", providersPage);

            // Get statistics
            model.addAttribute("totalProviders", getTotalProviders());
            model.addAttribute("activeProviders", getActiveProviders());
            model.addAttribute("totalVehicles", getTotalVehicles());
            model.addAttribute("averageRating", getAverageRating());

        } catch (Exception e) {
            System.err.println("Error fetching providers: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load providers");
        }

        return "admin/providers";
    }

    /**
     * Get providers page with filters
     */

    private Page<Provider> getProvidersPage(int page, int size,
                                            String name, String serviceType,
                                            String city, Double minRating) {
        return providerRepository.findAll(
                (root, query, cb) -> {
                    var predicates = new ArrayList<Predicate>();
                    if (name != null && !name.isEmpty()) {
                        predicates.add(
                                cb.or(
                                        cb.like(root.get("companyName"), "%" + name + "%"),
                                        cb.like(root.get("firstName"), "%" + name + "%"),
                                        cb.like(root.get("lastName"), "%" + name + "%")
                                )
                        );
                    }
                    if (serviceType != null && !serviceType.isEmpty()) {
                        predicates.add(cb.like(root.get("serviceTypes").as(String.class), "%" + serviceType + "%"));
                    }
                    if (city != null && !city.isEmpty()) {
                        predicates.add(cb.like(root.get("city"), "%" + city + "%"));
                    }
                    if (minRating != null) {
                        predicates.add(cb.ge(root.get("rating"), minRating));
                    }
                    return cb.and(predicates.toArray(new Predicate[0]));
                },
                PageRequest.of(page, size)
        );
    }

    /**
     * Row mapper for Provider entity
     */
    private static class ProviderRowMapper implements RowMapper<Provider> {
        @Override
        public Provider mapRow(ResultSet rs, int rowNum) throws SQLException {
            Provider provider = new Provider();
            provider.setId(rs.getLong("id"));
            provider.setFirstName(rs.getString("first_name"));
            provider.setLastName(rs.getString("last_name"));
            provider.setEmail(rs.getString("email"));
            provider.setUsername(rs.getString("username"));
            provider.setPhone(rs.getString("phone"));
            provider.setCity(rs.getString("city"));
            provider.setState(rs.getString("state"));
            provider.setCountry(rs.getString("country"));
            provider.setCompanyName(rs.getString("company_name"));
            provider.setCompanyAddress(rs.getString("company_address"));
            provider.setRating(rs.getDouble("rating"));
            provider.setNumberOfRatings(rs.getInt("number_of_ratings"));
            provider.setEmailVerified(rs.getBoolean("email_verified"));
            provider.setTransportCount(rs.getInt("transport_count"));
            provider.setCompanyLogoUrl(rs.getString("company_logo_url"));
            return provider;
        }
    }

    /**
     * Get total number of providers
     */
    private Long getTotalProviders() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM provider", Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get active providers (email verified)
     */
    private Long getActiveProviders() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM provider WHERE email_verified = true",
                    Long.class
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get total vehicles
     */
    private Long getTotalVehicles() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transport", Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get average rating
     */
    private String getAverageRating() {
        try {
            Double avg = jdbcTemplate.queryForObject(
                    "SELECT AVG(rating) FROM provider WHERE rating > 0",
                    Double.class
            );
            return avg != null ? String.format("%.1f", avg) : "0.0";
        } catch (Exception e) {
            return "0.0";
        }
    }

    /**
     * View single provider details
     */
    @GetMapping("/{id}")
    public String viewProvider(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            Provider provider = providerRepository.findById(id).orElse(null);
            if (provider == null) {
                return "redirect:/admin/providers";
            }

            model.addAttribute("provider", provider);
            model.addAttribute("vehicleCount", provider.getTransportCount() != null ? provider.getTransportCount() : 0);

            // If you have drivers linked via relationship, fetch them from JPA instead of raw SQL
            // Example: provider.getDrivers().size()
            // For now, we’ll leave driverCount as 0 or TODO
            model.addAttribute("driverCount", 0);

            return "admin/provider-details";
        } catch (Exception e) {
            System.err.println("Error fetching provider details: " + e.getMessage());
            return "redirect:/admin/providers";
        }
    }

    /**
     * Delete provider
     */
    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteProvider(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            String sql = "DELETE FROM provider WHERE id = ?";
            jdbcTemplate.update(sql, id);
            return ResponseEntity.ok(Map.of("message", "Provider deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting provider: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete provider"));
        }
    }

    /**
     * Get providers statistics as JSON
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProvidersStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProviders", getTotalProviders());
        stats.put("activeProviders", getActiveProviders());
        stats.put("totalVehicles", getTotalVehicles());
        stats.put("averageRating", getAverageRating());

        return ResponseEntity.ok(stats);
    }

    /**
     * Export providers to CSV
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportProviders(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Company Name,Contact Name,Email,Phone,City,State,Rating,Vehicles,Status\n");

            String sql = "SELECT * FROM provider ORDER BY id";
            List<Provider> providers = jdbcTemplate.query(sql, new ProviderRowMapper());

            for (Provider p : providers) {
                csv.append(p.getId()).append(",");
                csv.append(p.getCompanyName() != null ? p.getCompanyName() : "").append(",");
                csv.append(p.getFirstName()).append(" ").append(p.getLastName()).append(",");
                csv.append(p.getEmail()).append(",");
                csv.append(p.getPhone() != null ? p.getPhone() : "").append(",");
                csv.append(p.getCity() != null ? p.getCity() : "").append(",");
                csv.append(p.getState() != null ? p.getState() : "").append(",");
                csv.append(p.getRating() != null ? p.getRating() : "0").append(",");
                csv.append(p.getTransportCount() != null ? p.getTransportCount() : "0").append(",");
                csv.append(p.isEmailVerified() ? "Active" : "Pending").append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=providers.csv");
            headers.add("Content-Type", "text/csv");

            return ResponseEntity.ok().headers(headers).body(csv.toString());
        } catch (Exception e) {
            System.err.println("Error exporting providers: " + e.getMessage());
            return ResponseEntity.status(500).body("Export failed");
        }
    }
}