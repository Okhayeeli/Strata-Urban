//package com.strataurban.strata.RestControllers.v2.admin;
//
//import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
//import com.strataurban.strata.Enums.BookingStatus;
//import com.strataurban.strata.Enums.EnumPriority;
//import com.strataurban.strata.Repositories.v2.BookingRepository;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@Controller
//@RequestMapping("/admin/bookings")
//public class AdminBookingEditController {
//
//    @Autowired
//    private BookingRepository bookingRepository;
//
//    /**
//     * Check if user is authenticated and is an admin
//     */
//    private boolean isAuthenticated(HttpSession session) {
//        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
//        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
//    }
//
//    /**
//     * Show edit booking form
//     */
//    @GetMapping("/{id}/edit")
//    public String showEditForm(
//            @PathVariable Long id,
//            HttpSession session,
//            Model model
//    ) {
//        if (!isAuthenticated(session)) {
//            return "redirect:/admin/login";
//        }
//
//        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
//        model.addAttribute("adminUser", adminUser);
//
//        try {
//            BookingRequest booking = bookingRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Booking not found"));
//
//            model.addAttribute("booking", booking);
//            model.addAttribute("statuses", BookingStatus.values());
//            model.addAttribute("priorities", EnumPriority.values());
//
//            return "admin/booking-edit";
//        } catch (Exception e) {
//            System.err.println("Error fetching booking for edit: " + e.getMessage());
//            e.printStackTrace();
//            return "redirect:/admin/bookings?error=booking_not_found";
//        }
//    }
//
//    /**
//     * Update booking - handles form submission
//     */
//    @PostMapping("/{id}/update")
//    public String updateBooking(
//            @PathVariable Long id,
//            @RequestParam BookingStatus status,
//            @RequestParam(required = false) EnumPriority priority,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime serviceDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupDateTime,
//            @RequestParam(required = false) String pickUpLocation,
//            @RequestParam(required = false) String destination,
//            @RequestParam(required = false) String city,
//            @RequestParam(required = false) String state,
//            @RequestParam(required = false) String additionalStops,
//            @RequestParam(required = false) Integer numberOfPassengers,
//            @RequestParam(required = false) String eventType,
//            @RequestParam(required = false) String vehiclePreferenceType,
//            @RequestParam(required = false) Boolean luggageNeeded,
//            @RequestParam(required = false) Double estimatedWeightKg,
//            @RequestParam(required = false) Double volumeCubicMeters,
//            @RequestParam(required = false) String supplyType,
//            @RequestParam(required = false) String packageSize,
//            @RequestParam(required = false) String additionalNotes,
//            @RequestParam(required = false) Boolean hasMultipleStops,
//            @RequestParam(required = false) Boolean isReturnTrip,
//            @RequestParam(required = false) Boolean timingFlexible,
//            HttpSession session
//    ) {
//        if (!isAuthenticated(session)) {
//            return "redirect:/admin/login";
//        }
//
//        try {
//            BookingRequest booking =