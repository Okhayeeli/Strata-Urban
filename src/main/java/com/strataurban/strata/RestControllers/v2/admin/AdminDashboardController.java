package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.DTOs.v2.LoginResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DecimalFormat;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DecimalFormat;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
            model.addAttribute("totalClients", formatNumber(totalClients));

            // Monthly Revenue (placeholder - implement based on your payment system)
            String monthlyRevenue = "₦0.00";
            model.addAttribute("monthlyRevenue", monthlyRevenue);

            // Additional stats for future use
            model.addAttribute("totalDrivers", getTotalDrivers());
            model.addAttribute("totalVehicles", getTotalVehicles());
            model.addAttribute("totalServiceAreas", getTotalServiceAreas());
            model.addAttribute("pendingBookings", getPendingBookings());

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
            String sql = "SELECT COUNT(*) FROM user WHERE role = 'DRIVER'";
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
}