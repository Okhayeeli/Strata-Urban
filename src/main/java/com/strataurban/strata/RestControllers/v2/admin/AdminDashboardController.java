package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserRepository userRepository;

    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Dashboard home page
     */
    @GetMapping({"/dashboard", "/"})
    public String showDashboard(HttpSession session, Model model) {
        // Check authentication
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        // Get admin user from session
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        // Get dashboard statistics
        try {
            // Total Bookings
            Long totalBookings = getTotalBookings();
            model.addAttribute("totalBookings", formatNumber(totalBookings));

            // Active Providers
            Long activeProviders = getActiveProviders();
            model.addAttribute("activeProviders", formatNumber(activeProviders));

            // Total Clients
            Long totalClients = getTotalClients();
            model.addAttribute("totalClients", userRepository.findByRoles(EnumRoles.CLIENT).size());

            // Monthly Revenue (placeholder - implement based on your payment system)
            String monthlyRevenue = "₦0.00";
            model.addAttribute("monthlyRevenue", monthlyRevenue);

            // Additional stats for future use
            model.addAttribute("totalDrivers", getTotalDrivers());
            model.addAttribute("totalVehicles", getTotalVehicles());
            model.addAttribute("totalServiceAreas", getTotalServiceAreas());
            model.addAttribute("pendingBookings", getPendingBookings());
            model.addAttribute("activeRoutes", getActiveRoutes());

            // After your existing stats, add:
            model.addAttribute("recentBookings", getRecentBookings());
            model.addAttribute("recentProviders", getRecentProviders());
            model.addAttribute("recentClients", getRecentClients());
//            model.addAttribute("recentSignUps", getRecentSignUps());

        } catch (Exception e) {
            // Log error and set default values
            System.err.println("Error fetching dashboard stats: " + e.getMessage());
            model.addAttribute("totalBookings", "0");
            model.addAttribute("activeProviders", "0");
            model.addAttribute("totalClients", "0");
            model.addAttribute("monthlyRevenue", "₦0.00");
        }

        return "admin/dashboard";
    }

    /**
     * Get total number of booking requests
     */
    private Long getTotalBookings() {
        try {
            String sql = "SELECT COUNT(*) FROM booking_request";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get number of active providers
     */
    private Long getActiveProviders() {
        try {
            String sql = "SELECT COUNT(*) FROM provider";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get total number of clients
     */
    private Long getTotalClients() {
        try {
            // Assuming clients have role 'CLIENT' in the user table
            String sql = "SELECT COUNT(*) FROM user WHERE role = 'CLIENT'";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get total number of drivers
     */
    private Long getTotalDrivers() {
        try {
            String sql = "SELECT COUNT(*) FROM user WHERE roles = 'DRIVER'";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get total number of vehicles
     */
    private Long getTotalVehicles() {
        try {
            String sql = "SELECT COUNT(*) FROM transport";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long getActiveRoutes(){
        try{
            String sql = "SELECT COUNT(*) FROM routes where is_enabled = true";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }


    /**
     * Get total number of service areas
     */
    private Long getTotalServiceAreas() {
        try {
            String sql = "SELECT COUNT(*) FROM service_area";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get number of pending bookings
     */
    private Long getPendingBookings() {
        try {
            String sql = "SELECT COUNT(*) FROM booking_request WHERE status = 'PENDING'";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Format number with commas
     */
    private String formatNumber(Long number) {
        if (number == null) return "0";
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }

    /**
     * Get dashboard statistics as JSON (for AJAX calls)
     */
    @GetMapping("/dashboard/stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return Map.of("error", "Unauthorized");
        }

        return Map.of(
                "totalBookings", getTotalBookings(),
                "activeProviders", getActiveProviders(),
                "totalClients", getTotalClients(),
                "totalDrivers", getTotalDrivers(),
                "totalVehicles", getTotalVehicles(),
                "totalServiceAreas", getTotalServiceAreas(),
                "pendingBookings", getPendingBookings()
        );
    }

    /**
     * Get recent booking requests (last 10)
     */
    private List<Map<String, Object>> getRecentBookings() {
        try {
            String sql = "SELECT br.*, " +
                    "CONCAT(u.first_name, ' ', u.last_name) AS client_name " +
                    "FROM booking_request br " +
                    "LEFT JOIN user u ON br.client_id = u.id " +
                    "ORDER BY br.created_date DESC " +
                    "LIMIT 10";

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get recent provider signups (last 5)
     */
    private List<Map<String, Object>> getRecentProviders() {
        try {
            String sql = "SELECT u.*, p.* FROM user u left Join provider p on p.id = u.id where u.roles = 'PROVIDER' ORDER BY u.created_date DESC LIMIT 5";

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get recent client signups (last 5)
     */
    private List<Map<String, Object>> getRecentClients() {
        try {
            String sql = "SELECT * FROM user " +
                    "WHERE roles = 'CLIENT' " +
                    "ORDER BY created_date DESC LIMIT 5";

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get booking trends data for chart
     */
    @GetMapping("/dashboard/booking-trends")
    @ResponseBody
    public Map<String, Object> getBookingTrends(@RequestParam(defaultValue = "week") String period) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;
            List<String> labels;

            // Determine period and labels
            if ("year".equalsIgnoreCase(period)) {
                startDate = now.minusMonths(12);
                labels = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
            } else if ("month".equalsIgnoreCase(period)) {
                startDate = now.minusDays(30);
                labels = generateDayLabels();
            } else { // week
                startDate = now.minusDays(7);
                labels = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
            }

            // Query booking counts grouped by date
            String sql = "SELECT DATE(created_date) as booking_date, COUNT(*) as count " +
                    "FROM booking_request " +
                    "WHERE created_date >= ? " +
                    "GROUP BY DATE(created_date) " +
                    "ORDER BY booking_date";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, startDate);

            // Initialize data array with zeros
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                data.add(0);
            }

            // Map database results to chart data
            for (Map<String, Object> row : results) {
                java.sql.Date sqlDate = (java.sql.Date) row.get("booking_date");
                LocalDateTime bookingDate = sqlDate.toLocalDate().atStartOfDay();
                int count = ((Number) row.get("count")).intValue();

                // Find the correct index based on period
                int index = -1;
                if ("year".equalsIgnoreCase(period)) {
                    // Map to month index (0-11)
                    index = bookingDate.getMonthValue() - 1;
                } else if ("month".equalsIgnoreCase(period)) {
                    // Map to day index (0-29)
                    long daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate.toLocalDate(), bookingDate.toLocalDate());
                    index = (int) daysSinceStart;
                } else { // week
                    // Map to day of week (0-6: Mon-Sun)
                    int dayOfWeek = bookingDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
                    index = dayOfWeek - 1; // Convert to 0-based index
                }

                // Add count to the appropriate index
                if (index >= 0 && index < data.size()) {
                    data.set(index, data.get(index) + count);
                }
            }

            return Map.of(
                    "labels", labels,
                    "data", data
            );
        } catch (Exception e) {
            e.printStackTrace(); // Add this to see what error is happening
            return Map.of(
                    "labels", List.of(),
                    "data", List.of()
            );
        }
    }

    /**
     * Get service distribution for pie/doughnut chart
     */
    @GetMapping("/dashboard/service-distribution")
    @ResponseBody
    public Map<String, Object> getServiceDistribution() {
        try {
            // Use snake_case column names for MySQL
            String sql = "SELECT " +
                    "SUM(CASE WHEN is_passenger = 1 THEN 1 ELSE 0 END) as passenger, " +
                    "SUM(CASE WHEN is_cargo = 1 THEN 1 ELSE 0 END) as cargo, " +
                    "SUM(CASE WHEN is_medical = 1 THEN 1 ELSE 0 END) as medical, " +
                    "SUM(CASE WHEN is_furniture = 1 THEN 1 ELSE 0 END) as furniture, " +
                    "SUM(CASE WHEN is_food = 1 THEN 1 ELSE 0 END) as food, " +
                    "SUM(CASE WHEN is_equipment = 1 THEN 1 ELSE 0 END) as equipment " +
                    "FROM booking_request";

            Map<String, Object> result = jdbcTemplate.queryForMap(sql);

            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();

            // Only include non-zero categories
            addIfNonZero(labels, data, result, "passenger", "Passenger");
            addIfNonZero(labels, data, result, "cargo", "Cargo");
            addIfNonZero(labels, data, result, "medical", "Medical");
            addIfNonZero(labels, data, result, "furniture", "Furniture");
            addIfNonZero(labels, data, result, "food", "Food");
            addIfNonZero(labels, data, result, "equipment", "Equipment");

            return Map.of(
                    "labels", labels,
                    "data", data
            );
        } catch (Exception e) {
            e.printStackTrace(); // See the actual error
            return Map.of(
                    "labels", List.of(),
                    "data", List.of()
            );
        }
    }

    // Helper method to reduce code duplication
    private void addIfNonZero(List<String> labels, List<Integer> data,
                              Map<String, Object> result, String key, String label) {
        Object value = result.get(key);
        if (value != null) {
            int count = ((Number) value).intValue();
            if (count > 0) {
                labels.add(label);
                data.add(count);
            }
        }
    }

    private List<String> generateDayLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 30 - 1; i >= 0; i--) {
            labels.add(LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("MMM dd")));
        }
        return labels;
    }
}