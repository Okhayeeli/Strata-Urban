package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.Passengers.Client;
import com.strataurban.strata.Repositories.v2.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/clients")
public class EnhancedAdminClientsController {

    @Autowired
    private ClientRepository clientRepository;

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
     * Display clients management page with filters
     */
    @GetMapping({"", "/"})
    public String showClients(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

            // Build specification for filtering
            Specification<Client> spec = Specification.where(null);

            if (search != null && !search.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("firstName")), "%" + search.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("lastName")), "%" + search.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
                        )
                );
            }

            if (verified != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("emailVerified"), verified)
                );
            }

            if (city != null && !city.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%")
                );
            }

            if (state != null && !state.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("state")), "%" + state.toLowerCase() + "%")
                );
            }

            Page<Client> clients = clientRepository.findAll(spec, pageable);
            model.addAttribute("clients", clients);

            // Get statistics
            model.addAttribute("totalClients", clientRepository.count());
            model.addAttribute("verifiedClients", countVerifiedClients(true));
            model.addAttribute("activeClients", countActiveClientsThisMonth());
            model.addAttribute("newClients", countNewClientsThisWeek());

        } catch (Exception e) {
            System.err.println("Error fetching clients: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("clients", Page.empty());
            model.addAttribute("totalClients", 0);
            model.addAttribute("verifiedClients", 0);
            model.addAttribute("activeClients", 0);
            model.addAttribute("newClients", 0);
        }

        return "admin/clients";
    }

    /**
     * View single client details with booking statistics
     */
    @GetMapping("/{id}")
    public String viewClient(
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
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            model.addAttribute("client", client);

            // Get booking statistics
            Map<String, Long> bookingStats = getClientBookingStats(id);
            model.addAttribute("totalBookings", bookingStats.get("total"));
            model.addAttribute("completedBookings", bookingStats.get("completed"));
            model.addAttribute("totalSpent", getClientTotalSpent(id));

            return "admin/client-detail";
        } catch (Exception e) {
            System.err.println("Error fetching client: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Client not found");
            return "redirect:/admin/clients";
        }
    }

    /**
     * Show edit client form
     */
    @GetMapping("/{id}/edit")
    public String editClientForm(
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
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            model.addAttribute("client", client);
            return "admin/client-edit";
        } catch (Exception e) {
            System.err.println("Error fetching client for edit: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Client not found");
            return "redirect:/admin/clients";
        }
    }

    /**
     * Update client
     */
    @PostMapping("/{id}/update")
    public String updateClient(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Boolean emailVerified,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            // Validate input
            if (firstName == null || firstName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "First name is required");
                return "redirect:/admin/clients/" + id + "/edit?error=validation";
            }

            if (lastName == null || lastName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Last name is required");
                return "redirect:/admin/clients/" + id + "/edit?error=validation";
            }

            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                redirectAttributes.addFlashAttribute("error", "Valid email is required");
                return "redirect:/admin/clients/" + id + "/edit?error=validation";
            }

            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            // Check if email is already taken by another client
            Client existingClient = clientRepository.findByEmail(email);
            if (existingClient != null && !existingClient.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Email already in use by another client");
                return "redirect:/admin/clients/" + id + "/edit?error=duplicate_email";
            }

            // Update client fields
            client.setFirstName(firstName.trim());
            client.setLastName(lastName.trim());
            client.setEmail(email.trim());

            if (phone != null && !phone.trim().isEmpty()) {
                client.setPhone(phone.trim());
            }

            if (city != null && !city.trim().isEmpty()) {
                client.setCity(city.trim());
            }

            if (state != null && !state.trim().isEmpty()) {
                client.setState(state.trim());
            }

            if (emailVerified != null) {
                client.setEmailVerified(emailVerified);
            }

            clientRepository.save(client);
            redirectAttributes.addFlashAttribute("success", "Client updated successfully");
            return "redirect:/admin/clients/" + id + "?success=updated";

        } catch (Exception e) {
            System.err.println("Error updating client: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update client: " + e.getMessage());
            return "redirect:/admin/clients/" + id + "/edit?error=update_failed";
        }
    }

    /**
     * Delete client
     */
    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteClient(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            // Check if client has any bookings
            Long bookingCount = getClientBookingStats(id).get("total");
            if (bookingCount > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Cannot delete client with existing bookings. Found " + bookingCount + " booking(s)."
                        ));
            }

            clientRepository.deleteById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Client deleted successfully"
            ));

        } catch (Exception e) {
            System.err.println("Error deleting client: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to delete client: " + e.getMessage()
                    ));
        }
    }

    /**
     * Get client statistics as JSON
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getClientStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalClients", clientRepository.count());
            stats.put("verifiedClients", countVerifiedClients(true));
            stats.put("unverifiedClients", countVerifiedClients(false));
            stats.put("activeClients", countActiveClientsThisMonth());
            stats.put("newClients", countNewClientsThisWeek());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics"));
        }
    }

    /**
     * Bulk delete clients (AJAX)
     */
    @PostMapping("/bulk-delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkDeleteClients(
            HttpSession session,
            @RequestBody Map<String, Object> request
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            @SuppressWarnings("unchecked")
            java.util.List<Integer> idsList = (java.util.List<Integer>) request.get("ids");

            if (idsList == null || idsList.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "No clients selected"));
            }

            int deletedCount = 0;
            int skippedCount = 0;
            java.util.List<String> skippedClients = new java.util.ArrayList<>();

            for (Integer idInt : idsList) {
                Long id = idInt.longValue();
                try {
                    // Check if client has bookings
                    Long bookingCount = getClientBookingStats(id).get("total");
                    if (bookingCount == 0) {
                        clientRepository.deleteById(id);
                        deletedCount++;
                    } else {
                        Client client = clientRepository.findById(id).orElse(null);
                        if (client != null) {
                            skippedClients.add(client.getFirstName() + " " + client.getLastName());
                        }
                        skippedCount++;
                    }
                } catch (Exception e) {
                    skippedCount++;
                }
            }

            String message = deletedCount + " client(s) deleted successfully";
            if (skippedCount > 0) {
                message += ". " + skippedCount + " client(s) skipped (has bookings)";
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", message,
                    "deleted", deletedCount,
                    "skipped", skippedCount,
                    "skippedClients", skippedClients
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Export clients data (AJAX)
     */
    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<java.util.List<Map<String, Object>>> exportClients(
            HttpSession session,
            @RequestParam(required = false) String format
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            java.util.List<Client> clients = clientRepository.findAll();
            java.util.List<Map<String, Object>> exportData = new java.util.ArrayList<>();

            for (Client client : clients) {
                Map<String, Object> clientData = new HashMap<>();
                clientData.put("id", client.getId());
                clientData.put("firstName", client.getFirstName());
                clientData.put("lastName", client.getLastName());
                clientData.put("email", client.getEmail());
                clientData.put("phone", client.getPhone());
                clientData.put("city", client.getCity());
                clientData.put("state", client.getState());
                clientData.put("emailVerified", client.isEmailVerified());
                clientData.put("memberSince", client.getLastPasswordChange());

                // Add booking stats
                Map<String, Long> bookingStats = getClientBookingStats(client.getId());
                clientData.put("totalBookings", bookingStats.get("total"));
                clientData.put("completedBookings", bookingStats.get("completed"));

                exportData.add(clientData);
            }

            return ResponseEntity.ok(exportData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Count verified/unverified clients
     */
    private Long countVerifiedClients(boolean verified) {
        try {
            return clientRepository.count((root, query, cb) ->
                    cb.equal(root.get("emailVerified"), verified)
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Count active clients this month
     */
    private Long countActiveClientsThisMonth() {
        try {
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0);

            return clientRepository.count((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("lastActivity"), startOfMonth)
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Count new clients this week
     */
    private Long countNewClientsThisWeek() {
        try {
            LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
            return clientRepository.count((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("lastPasswordChange"), startOfWeek)
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get client booking statistics
     */
    private Map<String, Long> getClientBookingStats(Long clientId) {
        Map<String, Long> stats = new HashMap<>();

        try {
            // Total bookings
            String totalSql = "SELECT COUNT(*) FROM booking_request WHERE client_id = ?";
            Long total = jdbcTemplate.queryForObject(totalSql, Long.class, clientId);
            stats.put("total", total != null ? total : 0L);

            // Completed bookings
            String completedSql = "SELECT COUNT(*) FROM booking_request WHERE client_id = ? AND status = 'COMPLETED'";
            Long completed = jdbcTemplate.queryForObject(completedSql, Long.class, clientId);
            stats.put("completed", completed != null ? completed : 0L);

            // Pending bookings
            String pendingSql = "SELECT COUNT(*) FROM booking_request WHERE client_id = ? AND status = 'PENDING'";
            Long pending = jdbcTemplate.queryForObject(pendingSql, Long.class, clientId);
            stats.put("pending", pending != null ? pending : 0L);

            // Cancelled bookings
            String cancelledSql = "SELECT COUNT(*) FROM booking_request WHERE client_id = ? AND status = 'CANCELLED'";
            Long cancelled = jdbcTemplate.queryForObject(cancelledSql, Long.class, clientId);
            stats.put("cancelled", cancelled != null ? cancelled : 0L);

        } catch (Exception e) {
            System.err.println("Error fetching booking stats: " + e.getMessage());
            stats.put("total", 0L);
            stats.put("completed", 0L);
            stats.put("pending", 0L);
            stats.put("cancelled", 0L);
        }

        return stats;
    }

    /**
     * Get client total spent
     */
    private Double getClientTotalSpent(Long clientId) {
        try {
            String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM booking_request " +
                    "WHERE client_id = ? AND status = 'COMPLETED'";
            Double total = jdbcTemplate.queryForObject(sql, Double.class, clientId);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            System.err.println("Error fetching total spent: " + e.getMessage());
            return 0.0;
        }
    }
}