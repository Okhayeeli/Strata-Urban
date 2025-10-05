package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.ServiceImpls.v2.UserServiceImpl;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminBookingController {

    @Autowired
    private BookingRepository bookingRequestRepository;

    @Autowired
    private UserServiceImpl userServiceImpl;
    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Display bookings management page with filters
     */
    @GetMapping("/bookings")
    public String showBookings(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String fromDate,
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
            // Create pageable with sorting by creation date descending
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

            // Build specification for filtering
            Specification<BookingRequest> spec = Specification.where(null);

            if (status != null && !status.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("status"), BookingStatus.valueOf(status))
                );
            }

            if (priority != null && !priority.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("priority"), EnumPriority.valueOf(priority))
                );
            }

            if (city != null && !city.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%")
                );
            }

            if (fromDate != null && !fromDate.isEmpty()) {
                LocalDateTime startDate = LocalDate.parse(fromDate).atStartOfDay();
                spec = spec.and((root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("serviceDate"), startDate)
                );
            }

            // Get filtered bookings
            Page<BookingRequest> bookings = bookingRequestRepository.findAll(spec, pageable);

            Map<Long, String> clientNames = new HashMap<>();

            for (BookingRequest booking : bookings) {
                try {
                    Long clientId = booking.getClientId();
                    String fullName = userServiceImpl.findFullNameById(clientId);
                    clientNames.put(booking.getId(), fullName);
                } catch (Exception e) {
                    clientNames.put(booking.getId(), "Unknown");
                }
            }
            model.addAttribute("bookings", bookings);
            model.addAttribute("clientNames", clientNames);
            model.addAttribute("bookings", bookings);

            // Get statistics
            model.addAttribute("totalBookings", bookingRequestRepository.count());
            model.addAttribute("pendingBookings", countByStatus(BookingStatus.PENDING));
            model.addAttribute("confirmedBookings", countByStatus(BookingStatus.CONFIRMED));
            model.addAttribute("completedBookings", countByStatus(BookingStatus.COMPLETED));

        } catch (Exception e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("bookings", Page.empty());
            model.addAttribute("totalBookings", 0);
            model.addAttribute("pendingBookings", 0);
            model.addAttribute("confirmedBookings", 0);
            model.addAttribute("completedBookings", 0);
        }

        return "admin/bookings";
    }

    /**
     * View single booking details
     */
    @GetMapping("/bookings/{id}")
    public String viewBooking(
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
            BookingRequest booking = bookingRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            model.addAttribute("booking", booking);
            return "admin/booking-detail";
        } catch (Exception e) {
            System.err.println("Error fetching booking: " + e.getMessage());
            return "redirect:/admin/bookings?error=booking_not_found";
        }
    }

    /**
     * Confirm booking
     */
    @PostMapping("/bookings/{id}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmBooking(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            BookingRequest booking = bookingRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRequestRepository.save(booking);

            return ResponseEntity.ok(Map.of("message", "Booking confirmed successfully"));
        } catch (Exception e) {
            System.err.println("Error confirming booking: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to confirm booking"));
        }
    }

    /**
     * Cancel booking
     */
    @PostMapping("/bookings/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            BookingRequest booking = bookingRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            booking.setStatus(BookingStatus.CANCELLED);
            bookingRequestRepository.save(booking);

            return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
        } catch (Exception e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to cancel booking"));
        }
    }

    /**
     * Get bookings grouped by provider
     */
    @GetMapping("/bookings/grouped-by-provider")
    @ResponseBody
    public ResponseEntity<?> getBookingsGroupedByProvider(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            List<BookingRequest> allBookings = bookingRequestRepository.findAll();
            Map<Long, List<BookingRequest>> groupedByProvider = allBookings.stream()
                    .filter(b -> b.getProviderId() != null)
                    .collect(java.util.stream.Collectors.groupingBy(BookingRequest::getProviderId));

            Map<Long, Map<String, Object>> result = new java.util.HashMap<>();
            groupedByProvider.forEach((providerId, bookings) -> {
                Map<String, Object> providerData = new java.util.HashMap<>();
                providerData.put("totalBookings", bookings.size());
                providerData.put("bookings", bookings);
                providerData.put("statusCounts", bookings.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                BookingRequest::getStatus,
                                java.util.stream.Collectors.counting()
                        ))
                );
                result.put(providerId, providerData);
            });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to group bookings"));
        }
    }

    /**
     * Show edit booking form
     */
    @GetMapping("/bookings/{id}/edit")
    public String editBookingForm(
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
            BookingRequest booking = bookingRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            model.addAttribute("booking", booking);
            model.addAttribute("statuses", BookingStatus.values());
            model.addAttribute("priorities", EnumPriority.values());
            return "admin/booking-edit";
        } catch (Exception e) {
            System.err.println("Error fetching booking for edit: " + e.getMessage());
            return "redirect:/admin/bookings?error=booking_not_found";
        }
    }

    /**
     * Update booking
     */
    @PostMapping("/bookings/{id}/update")
    public String updateBooking(
            @PathVariable Long id,
            @RequestParam BookingStatus status,
            @RequestParam(required = false) EnumPriority priority,
            @RequestParam(required = false) String additionalNotes,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        try {
            BookingRequest booking = bookingRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            booking.setStatus(status);
            if (priority != null) {
                booking.setPriority(priority);
            }
            if (additionalNotes != null && !additionalNotes.isEmpty()) {
                booking.setAdditionalNotes(additionalNotes);
            }

            bookingRequestRepository.save(booking);
            return "redirect:/admin/bookings/" + id + "?success=updated";
        } catch (Exception e) {
            System.err.println("Error updating booking: " + e.getMessage());
            return "redirect:/admin/bookings/" + id + "/edit?error=update_failed";
        }
    }

    /**
     * Delete booking
     */
    @PostMapping("/bookings/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteBooking(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            bookingRequestRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Booking deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete booking"));
        }
    }

    /**
     * Get booking statistics as JSON
     */
    @GetMapping("/bookings/stats")
    @ResponseBody
    public ResponseEntity<?> getBookingStats(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            return ResponseEntity.ok(Map.of(
                    "totalBookings", bookingRequestRepository.count(),
                    "pendingBookings", countByStatus(BookingStatus.PENDING),
                    "confirmedBookings", countByStatus(BookingStatus.CONFIRMED),
                    "completedBookings", countByStatus(BookingStatus.COMPLETED),
                    "cancelledBookings", countByStatus(BookingStatus.CANCELLED)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch statistics"));
        }
    }

    /**
     * Helper method to count bookings by status
     */
    private Long countByStatus(BookingStatus status) {
        try {
            return bookingRequestRepository.count((root, query, cb) ->
                    cb.equal(root.get("status"), status)
            );
        } catch (Exception e) {
            return 0L;
        }
    }
}