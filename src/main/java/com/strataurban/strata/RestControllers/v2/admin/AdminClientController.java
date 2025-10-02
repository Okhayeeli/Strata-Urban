package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.Passengers.Client;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.ClientRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
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
import org.springframework.http.HttpHeaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/clients")
public class AdminClientController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ClientRepository clientRepository;

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
     * Show clients list page
     */
    @GetMapping({"", "/"})
    public String showClientsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            HttpSession session,
            Model model
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            // Fetch clients from database with pagination
            Page<User> clientsPage = getClientsPage(page, size, name, city);
            model.addAttribute("clients", clientsPage); // Pass the Page object, not just content

            // Get statistics
            model.addAttribute("totalClients", getTotalClients());
            model.addAttribute("activeClients", getActiveClients());
            model.addAttribute("totalBookings", getTotalBookings());

        } catch (Exception e) {
            System.err.println("Error fetching clients: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load clients");
        }

        return "admin/clients";
    }

    /**
     * Get clients page with filters
     */
    private Page<User> getClientsPage(int page, int size, String name, String city) {
        return userRepository.findAll(
                (root, query, cb) -> {
                    var predicates = new ArrayList<Predicate>();
                    predicates.add(cb.equal(root.get("roles"), EnumRoles.CLIENT));
                    if (name != null && !name.isEmpty()) {
                        predicates.add(
                                cb.or(
                                        cb.like(root.get("firstName"), "%" + name + "%"),
                                        cb.like(root.get("lastName"), "%" + name + "%")
                                )
                        );
                    }
                    if (city != null && !city.isEmpty()) {
                        predicates.add(cb.like(root.get("city"), "%" + city + "%"));
                    }
                    return cb.and(predicates.toArray(new Predicate[0]));
                },
                PageRequest.of(page, size)
        );
    }

    /**
     * Row mapper for Client entity
     */
    private static class ClientRowMapper implements RowMapper<Client> {
        @Override
        public Client mapRow(ResultSet rs, int rowNum) throws SQLException {
            Client client = new Client();
            client.setId(rs.getLong("id"));
            client.setTitle(rs.getString("title"));
            client.setFirstName(rs.getString("first_name"));
            client.setMiddleName(rs.getString("middle_name"));
            client.setLastName(rs.getString("last_name"));
            client.setEmail(rs.getString("email"));
            client.setUsername(rs.getString("username"));
            client.setPhone(rs.getString("phone"));
            client.setCity(rs.getString("city"));
            client.setState(rs.getString("state"));
            client.setCountry(rs.getString("country"));
            client.setEmailVerified(rs.getBoolean("email_verified"));
            client.setAddress(rs.getString("address"));
            client.setPreferredLanguage(rs.getString("preferred_language"));
            return client;
        }
    }

    /**
     * Get total number of clients
     */
    private Long getTotalClients() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE roles = 'CLIENT'", Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get active clients (email verified)
     */
    private Long getActiveClients() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user WHERE roles = 'CLIENT' AND email_verified = true",
                    Long.class
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get total bookings
     */
    private Long getTotalBookings() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM booking", Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * View single client details
     */
    @GetMapping("/{id}")
    public String viewClient(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            User client = userRepository.findByIdAndRoles(id, EnumRoles.CLIENT).orElseThrow();
            if (client == null) {
                return "redirect:/admin/clients";
            }

            model.addAttribute("client", client);

            List<BookingRequest> bookings = bookingRepository.findByClientId(id);
            model.addAttribute("bookings", bookings);
            model.addAttribute("totalBookings", bookings.size());
            model.addAttribute("completedBookings", bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count());
//            double totalSpent = bookings.stream().mapToDouble(b -> b.getAmount() != null ? b.getAmount() : 0).sum();
            model.addAttribute("totalSpent", String.format("%.2f", 879.90));

            return "admin/client-details";
        } catch (Exception e) {
            System.err.println("Error fetching client details: " + e.getMessage());
            return "redirect:/admin/clients";
        }
    }

    /**
     * Delete client
     */
    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteClient(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            String sql = "DELETE FROM user WHERE id = ? AND roles = 'CLIENT'";
            jdbcTemplate.update(sql, id);
            return ResponseEntity.ok(Map.of("message", "Client deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting client: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete client"));
        }
    }

    /**
     * Get clients statistics as JSON
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getClientsStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClients", getTotalClients());
        stats.put("activeClients", getActiveClients());
        stats.put("totalBookings", getTotalBookings());

        return ResponseEntity.ok(stats);
    }

    /**
     * Export clients to CSV
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportClients(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Full Name,Email,Phone,City,State,Status\n");

            String sql = "SELECT * FROM user WHERE roles = 'CLIENT' ORDER BY id";
            List<Client> clients = jdbcTemplate.query(sql, new ClientRowMapper());

            for (Client c : clients) {
                csv.append(c.getId()).append(",");
                csv.append(c.getTitle() != null ? c.getTitle() + " " : "")
                        .append(c.getFirstName()).append(" ")
                        .append(c.getMiddleName() != null ? c.getMiddleName() + " " : "")
                        .append(c.getLastName()).append(",");
                csv.append(c.getEmail()).append(",");
                csv.append(c.getPhone() != null ? c.getPhone() : "").append(",");
                csv.append(c.getCity() != null ? c.getCity() : "").append(",");
                csv.append(c.getState() != null ? c.getState() : "").append(",");
                csv.append(c.isEmailVerified() ? "Active" : "Pending").append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=clients.csv");
            headers.add("Content-Type", "text/csv");

            return ResponseEntity.ok().headers(headers).body(csv.toString());
        } catch (Exception e) {
            System.err.println("Error exporting clients: " + e.getMessage());
            return ResponseEntity.status(500).body("Export failed");
        }
    }
}