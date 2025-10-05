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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/admin/v2")
public class AdminClientsController {

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
     * Display clients management page with filters
     */
    @GetMapping("/clients")
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
        // Check authentication
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        // Get admin user from session
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            // Create pageable with sorting by ID descending
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

            // Build specification for filtering
            Specification<Client> spec = Specification.where(null);

            // Search filter (name or email)
            if (search != null && !search.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("firstName")), "%" + search.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("lastName")), "%" + search.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
                        )
                );
            }

            // Verification status filter
            if (verified != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("emailVerified"), verified)
                );
            }

            // City filter
            if (city != null && !city.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%")
                );
            }

            // State filter
            if (state != null && !state.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("state")), "%" + state.toLowerCase() + "%")
                );
            }

            // Get filtered clients
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
     * View single client details
     */
    @GetMapping("/clients/{id}")
    public String viewClient(
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
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            model.addAttribute("client", client);
            return "admin/client-detail";
        } catch (Exception e) {
            System.err.println("Error fetching client: " + e.getMessage());
            return "redirect:/admin/clients?error=client_not_found";
        }
    }

    /**
     * Show edit client form
     */
    @GetMapping("/clients/{id}/edit")
    public String editClientForm(
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
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            model.addAttribute("client", client);
            return "admin/client-edit";
        } catch (Exception e) {
            System.err.println("Error fetching client for edit: " + e.getMessage());
            return "redirect:/admin/clients?error=client_not_found";
        }
    }

    /**
     * Update client
     */
    @PostMapping("/clients/{id}/update")
    public String updateClient(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Boolean emailVerified,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            client.setFirstName(firstName);
            client.setLastName(lastName);
            client.setEmail(email);

            if (phone != null && !phone.isEmpty()) {
                client.setPhone(phone);
            }
            if (city != null && !city.isEmpty()) {
                client.setCity(city);
            }
            if (state != null && !state.isEmpty()) {
                client.setState(state);
            }
            if (emailVerified != null) {
                client.setEmailVerified(emailVerified);
            }

            clientRepository.save(client);
            return "redirect:/admin/clients/" + id + "?success=updated";
        } catch (Exception e) {
            System.err.println("Error updating client: " + e.getMessage());
            return "redirect:/admin/clients/" + id + "/edit?error=update_failed";
        }
    }

    /**
     * Delete client
     */
    @PostMapping("/clients/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteClient(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            clientRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Client deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting client: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete client"));
        }
    }

    /**
     * Get client statistics as JSON
     */
    @GetMapping("/clients/stats")
    @ResponseBody
    public ResponseEntity<?> getClientStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            return ResponseEntity.ok(Map.of(
                    "totalClients", clientRepository.count(),
                    "verifiedClients", countVerifiedClients(true),
                    "unverifiedClients", countVerifiedClients(false),
                    "activeClients", countActiveClientsThisMonth(),
                    "newClients", countNewClientsThisWeek()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch statistics"));
        }
    }

    /**
     * Helper method to count verified/unverified clients
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
     * Helper method to count active clients this month
     */
    private Long countActiveClientsThisMonth() {
        try {
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            return clientRepository.count((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("lastActivity"), startOfMonth)
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Helper method to count new clients this week
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
}