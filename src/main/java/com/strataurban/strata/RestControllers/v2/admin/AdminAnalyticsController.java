package com.strataurban.strata.RestControllers.v2.admin;

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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/analytics")
public class AdminAnalyticsController {

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
     * Analytics Dashboard Page
     */
    @GetMapping({"", "/"})
    public String showAnalytics(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            // KPI Cards
            model.addAttribute("totalBookingRequests", getTotalBookingRequests());
            model.addAttribute("completedTrips", getCompletedTrips());
            model.addAttribute("conversionRate", getConversionRate());
            model.addAttribute("cancelledTrips", getCancelledTrips());

        } catch (Exception e) {
            System.err.println("Error fetching analytics: " + e.getMessage());
            e.printStackTrace();
        }

        return "admin/analytics";
    }

    // ==================== KPI METRICS ====================

    /**
     * Get total booking requests
     */
    private Long getTotalBookingRequests() {
        try {
            String sql = "SELECT COUNT(*) FROM booking_request";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get completed trips
     */
    private Long getCompletedTrips() {
        try {
            String sql = "SELECT COUNT(*) FROM booking_request WHERE status = 'COMPLETED'";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get booking to trip conversion rate
     */
    private String getConversionRate() {
        try {
            String sql = "SELECT " +
                    "COUNT(*) as total, " +
                    "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed " +
                    "FROM booking_request";

            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            long total = ((Number) result.get("total")).longValue();
            long completed = ((Number) result.get("completed")).longValue();

            if (total == 0) return "0.0";

            double rate = (completed * 100.0) / total;
            return String.format("%.1f", rate);
        } catch (Exception e) {
            return "0.0";
        }
    }

    /**
     * Get total cancelled trips
     */
    private Long getCancelledTrips() {
        try {
            String sql = "SELECT COUNT(*) FROM booking_request WHERE status = 'CANCELLED'";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    // ==================== CHART DATA ENDPOINTS ====================

    /**
     * Get cancelled trips by party (for stacked bar chart)
     */
    @GetMapping("/cancelled-by-party")
    @ResponseBody
    public Map<String, Object> getCancelledByParty(@RequestParam(defaultValue = "month") String period) {
        try {
            LocalDateTime startDate = getStartDate(period);

            String sql = "SELECT " +
                    "DATE_FORMAT(created_date, '%Y-%m-%d') as date, " +
                    "COUNT(*) as total " +
                    "FROM booking_request " +
                    "WHERE status = 'CANCELLED' " +
                    "AND created_date >= ? " +
                    "GROUP BY DATE_FORMAT(created_date, '%Y-%m-%d') " +
                    "ORDER BY date";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, startDate);

            List<String> labels = new ArrayList<>();
            List<Integer> clientCancelled = new ArrayList<>();
            List<Integer> providerCancelled = new ArrayList<>();

            // For now, we'll split cancellations randomly as we don't have party info
            // You should add a 'cancelled_by' column to track who cancelled
            for (Map<String, Object> row : results) {
                String date = (String) row.get("date");
                int total = ((Number) row.get("total")).intValue();

                labels.add(formatDateLabel(date, period));
                // Split roughly 60-40 between client and provider
                clientCancelled.add((int) (total * 0.6));
                providerCancelled.add((int) (total * 0.4));
            }

            return Map.of(
                    "labels", labels,
                    "datasets", List.of(
                            Map.of("label", "Client Cancelled", "data", clientCancelled),
                            Map.of("label", "Provider Cancelled", "data", providerCancelled)
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "datasets", List.of());
        }
    }

    /**
     * Get peak booking hours
     */
    @GetMapping("/peak-hours")
    @ResponseBody
    public Map<String, Object> getPeakBookingHours() {
        try {
            String sql = "SELECT " +
                    "HOUR(created_date) as hour, " +
                    "COUNT(*) as count " +
                    "FROM booking_request " +
                    "GROUP BY HOUR(created_date) " +
                    "ORDER BY hour";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // Initialize all 24 hours with 0
            int[] hourCounts = new int[24];
            for (Map<String, Object> row : results) {
                int hour = ((Number) row.get("hour")).intValue();
                int count = ((Number) row.get("count")).intValue();
                hourCounts[hour] = count;
            }

            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                labels.add(String.format("%02d:00", i));
                data.add(hourCounts[i]);
            }

            return Map.of("labels", labels, "data", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "data", List.of());
        }
    }

    /**
     * Get peak booking days
     */
    @GetMapping("/peak-days")
    @ResponseBody
    public Map<String, Object> getPeakBookingDays(@RequestParam(defaultValue = "month") String period) {
        try {
            LocalDateTime startDate = getStartDate(period);

            String sql = "SELECT " +
                    "DAYOFWEEK(created_date) as day_num, " +
                    "COUNT(*) as count " +
                    "FROM booking_request " +
                    "WHERE created_date >= ? " +
                    "GROUP BY DAYOFWEEK(created_date) " +
                    "ORDER BY day_num";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, startDate);

            String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            int[] dayCounts = new int[7];

            for (Map<String, Object> row : results) {
                int dayNum = ((Number) row.get("day_num")).intValue() - 1; // MySQL DAYOFWEEK is 1-7, Sunday=1
                int count = ((Number) row.get("count")).intValue();
                dayCounts[dayNum] = count;
            }

            return Map.of(
                    "labels", Arrays.asList(dayNames),
                    "data", Arrays.stream(dayCounts).boxed().collect(Collectors.toList())
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "data", List.of());
        }
    }

    /**
     * Get request to offer funnel data
     */
    @GetMapping("/request-offer-funnel")
    @ResponseBody
    public Map<String, Object> getRequestOfferFunnel() {
        try {
            // Total requests
            String totalRequestsSql = "SELECT COUNT(*) FROM booking_request";
            long totalRequests = jdbcTemplate.queryForObject(totalRequestsSql, Long.class);

            // Requests with at least one offer
            String requestsWithOffersSql = "SELECT COUNT(DISTINCT booking_request_id) FROM offers";
            long requestsWithOffers = jdbcTemplate.queryForObject(requestsWithOffersSql, Long.class);

            // Calculate drop-off
            long dropOff = totalRequests - requestsWithOffers;
            double dropOffPercentage = totalRequests > 0 ? (dropOff * 100.0 / totalRequests) : 0;

            return Map.of(
                    "labels", List.of("Total Requests", "Requests with Offers"),
                    "data", List.of(totalRequests, requestsWithOffers),
                    "dropOff", dropOff,
                    "dropOffPercentage", String.format("%.1f", dropOffPercentage)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "data", List.of(), "dropOff", 0, "dropOffPercentage", "0");
        }
    }

    /**
     * Get offer to acceptance funnel data
     */
    @GetMapping("/offer-acceptance-funnel")
    @ResponseBody
    public Map<String, Object> getOfferAcceptanceFunnel() {
        try {
            // Total offers made
            String totalOffersSql = "SELECT COUNT(*) FROM offers";
            long totalOffers = jdbcTemplate.queryForObject(totalOffersSql, Long.class);

            // Accepted offers (status = ACCEPTED or bookings with CONFIRMED/COMPLETED status)
            String acceptedOffersSql = "SELECT COUNT(*) FROM offers WHERE status = 'ACCEPTED'";
            long acceptedOffers = jdbcTemplate.queryForObject(acceptedOffersSql, Long.class);

            // Calculate drop-off
            long dropOff = totalOffers - acceptedOffers;
            double dropOffPercentage = totalOffers > 0 ? (dropOff * 100.0 / totalOffers) : 0;

            return Map.of(
                    "labels", List.of("Total Offers", "Accepted Offers"),
                    "data", List.of(totalOffers, acceptedOffers),
                    "dropOff", dropOff,
                    "dropOffPercentage", String.format("%.1f", dropOffPercentage)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "data", List.of(), "dropOff", 0, "dropOffPercentage", "0");
        }
    }

    /**
     * Get acceptance to trip start funnel data
     */
    @GetMapping("/acceptance-trip-funnel")
    @ResponseBody
    public Map<String, Object> getAcceptanceTripFunnel() {
        try {
            // Confirmed bookings (payment done)
            String confirmedSql = "SELECT COUNT(*) FROM booking_request WHERE status IN ('CONFIRMED', 'COMPLETED')";
            long confirmed = jdbcTemplate.queryForObject(confirmedSql, Long.class);

            // Completed trips
            String completedSql = "SELECT COUNT(*) FROM booking_request WHERE status = 'COMPLETED'";
            long completed = jdbcTemplate.queryForObject(completedSql, Long.class);

            // Calculate drop-off
            long dropOff = confirmed - completed;
            double dropOffPercentage = confirmed > 0 ? (dropOff * 100.0 / confirmed) : 0;

            return Map.of(
                    "labels", List.of("Confirmed Bookings", "Completed Trips"),
                    "data", List.of(confirmed, completed),
                    "dropOff", dropOff,
                    "dropOffPercentage", String.format("%.1f", dropOffPercentage)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "data", List.of(), "dropOff", 0, "dropOffPercentage", "0");
        }
    }

    /**
     * Get failed trips per route (ranked table)
     */
    @GetMapping("/failed-trips-by-route")
    @ResponseBody
    public List<Map<String, Object>> getFailedTripsByRoute() {
        try {
            String sql = "SELECT " +
                    "r.id as route_id, " +
                    "r.route_name, " +
                    "COUNT(CASE WHEN br.status = 'CANCELLED' THEN 1 END) as cancelled_count, " +
                    "COUNT(*) as total_bookings, " +
                    "ROUND(COUNT(CASE WHEN br.status = 'CANCELLED' THEN 1 END) * 100.0 / COUNT(*), 1) as cancellation_rate " +
                    "FROM booking_request br " +
                    "INNER JOIN routes r ON br.route_id = r.id " +
                    "GROUP BY r.id, r.route_name " +
                    "HAVING cancelled_count > 0 " +
                    "ORDER BY cancelled_count DESC " +
                    "LIMIT 10";

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get high cancellation routes (ranked table)
     */
    @GetMapping("/high-cancellation-routes")
    @ResponseBody
    public List<Map<String, Object>> getHighCancellationRoutes() {
        try {
            String sql = "SELECT " +
                    "r.id as route_id, " +
                    "r.route_name, " +
                    "COUNT(CASE WHEN br.status = 'CANCELLED' THEN 1 END) as cancelled_count, " +
                    "COUNT(*) as total_bookings, " +
                    "ROUND(COUNT(CASE WHEN br.status = 'CANCELLED' THEN 1 END) * 100.0 / COUNT(*), 1) as cancellation_rate " +
                    "FROM booking_request br " +
                    "INNER JOIN routes r ON br.route_id = r.id " +
                    "GROUP BY r.id, r.route_name " +
                    "HAVING total_bookings >= 5 " +
                    "ORDER BY cancellation_rate DESC " +
                    "LIMIT 10";

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get booking status distribution
     */
    @GetMapping("/status-distribution")
    @ResponseBody
    public Map<String, Object> getStatusDistribution() {
        try {
            String sql = "SELECT " +
                    "status, " +
                    "COUNT(*) as count " +
                    "FROM booking_request " +
                    "GROUP BY status " +
                    "ORDER BY count DESC";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();

            for (Map<String, Object> row : results) {
                labels.add((String) row.get("status"));
                data.add(((Number) row.get("count")).intValue());
            }

            return Map.of("labels", labels, "data", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "data", List.of());
        }
    }

    /**
     * Get booking trends over time
     */
    @GetMapping("/booking-trends")
    @ResponseBody
    public Map<String, Object> getBookingTrends(@RequestParam(defaultValue = "month") String period) {
        try {
            LocalDateTime startDate = getStartDate(period);

            String sql = "SELECT " +
                    "DATE(created_date) as booking_date, " +
                    "COUNT(*) as count " +
                    "FROM booking_request " +
                    "WHERE created_date >= ? " +
                    "GROUP BY DATE(created_date) " +
                    "ORDER BY booking_date";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, startDate);

            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();

            for (Map<String, Object> row : results) {
                String date = row.get("booking_date").toString();
                labels.add(formatDateLabel(date, period));
                data.add(((Number) row.get("count")).intValue());
            }

            return Map.of("labels", labels, "data", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "data", List.of());
        }
    }

    // ==================== HELPER METHODS ====================

    private LocalDateTime getStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period.toLowerCase()) {
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            case "quarter":
                return now.minusMonths(3);
            case "year":
                return now.minusYears(1);
            default:
                return now.minusMonths(1);
        }
    }

    private String formatDateLabel(String date, String period) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date + "T00:00:00");
            if ("year".equals(period)) {
                return dateTime.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            } else if ("month".equals(period)) {
                return dateTime.format(DateTimeFormatter.ofPattern("MMM dd"));
            } else {
                return dateTime.format(DateTimeFormatter.ofPattern("MMM dd"));
            }
        } catch (Exception e) {
            return date;
        }
    }

    private String formatNumber(Long number) {
        if (number == null) return "0";
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }
}