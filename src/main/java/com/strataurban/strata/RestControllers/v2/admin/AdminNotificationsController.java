package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Notifications.NotificationChannel;
import com.strataurban.strata.Notifications.NotificationPreferenceService;
import com.strataurban.strata.Repositories.v2.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationsController {

    private final UserRepository userRepository;
    private final NotificationPreferenceService preferenceService;

    /**
     * Check if user is authenticated and is an admin
     */
    private boolean isAuthenticated(HttpSession session) {
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        return adminUser != null && "ADMIN".equalsIgnoreCase((String) adminUser.get("role"));
    }

    /**
     * Display notifications management page
     */
    @GetMapping
    public String showNotificationsPage(
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean emailEnabled,
            @RequestParam(required = false) Boolean smsEnabled,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        // Check authentication
        if (!isAuthenticated(session)) {
            return "redirect:/admin/login";
        }

        // Get admin user from session
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        model.addAttribute("adminUser", adminUser);

        try {
            // Build specification for filtering
            Specification<User> spec = Specification.where(null);

            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                spec = spec.and((root, query, cb) -> cb.or(
                        cb.like(cb.lower(root.get("firstName")), "%" + searchLower + "%"),
                        cb.like(cb.lower(root.get("lastName")), "%" + searchLower + "%"),
                        cb.like(cb.lower(root.get("email")), "%" + searchLower + "%"),
                        cb.like(cb.lower(cb.toString(root.get("id"))), "%" + searchLower + "%")
                ));
            }

            // Role filter
            if (role != null && !role.trim().isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("roles"), com.strataurban.strata.Enums.EnumRoles.valueOf(role))
                );
            }

            // Create pageable with sorting
            Sort sort = sortDir.equalsIgnoreCase("DESC")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch users
            Page<User> usersPage = userRepository.findAll(spec, pageable);

            // Get all user IDs for batch fetching preferences
            List<Long> userIds = usersPage.getContent().stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            // Fetch preferences for all users in this page
            Map<Long, Map<String, Boolean>> userPreferences = new HashMap<>();
            for (Long userId : userIds) {
                Map<NotificationChannel, Boolean> prefs = preferenceService.getUserPreferencesMap(userId);

                // Convert to String keys for Thymeleaf
                Map<String, Boolean> prefsMap = new HashMap<>();
                prefsMap.put("EMAIL", prefs.get(NotificationChannel.EMAIL));
                prefsMap.put("SMS", prefs.get(NotificationChannel.SMS));
                prefsMap.put("PUSH", prefs.get(NotificationChannel.PUSH));
                prefsMap.put("IN_APP", prefs.get(NotificationChannel.IN_APP));

                userPreferences.put(userId, prefsMap);
            }

            // Calculate statistics
            long totalUsers = userRepository.count();
            long emailEnabledCount = calculateEnabledCount(NotificationChannel.EMAIL);
            long smsEnabledCount = calculateEnabledCount(NotificationChannel.SMS);
            long pushEnabledCount = calculateEnabledCount(NotificationChannel.PUSH);
            long inAppEnabledCount = calculateEnabledCount(NotificationChannel.IN_APP);

            // Add to model
            model.addAttribute("users", usersPage);
            model.addAttribute("userPreferences", userPreferences);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("emailEnabledCount", emailEnabledCount);
            model.addAttribute("smsEnabledCount", smsEnabledCount);
            model.addAttribute("pushEnabledCount", pushEnabledCount);
            model.addAttribute("inAppEnabledCount", inAppEnabledCount);

            // Add filter parameters back to model for form
            model.addAttribute("param", Map.of(
                    "search", search != null ? search : "",
                    "role", role != null ? role : "",
                    "emailEnabled", emailEnabled != null ? emailEnabled.toString() : "",
                    "smsEnabled", smsEnabled != null ? smsEnabled.toString() : "",
                    "sortBy", sortBy,
                    "sortDir", sortDir
            ));

        } catch (Exception e) {
            log.error("Error loading notifications page", e);
            model.addAttribute("error", "Failed to load notification preferences: " + e.getMessage());

            // Set empty defaults
            model.addAttribute("users", Page.empty());
            model.addAttribute("userPreferences", new HashMap<>());
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("emailEnabledCount", 0L);
            model.addAttribute("smsEnabledCount", 0L);
            model.addAttribute("pushEnabledCount", 0L);
            model.addAttribute("inAppEnabledCount", 0L);
        }

        return "admin/notifications-admin";
    }

    /**
     * Calculate how many users have a specific channel enabled
     */
    private long calculateEnabledCount(NotificationChannel channel) {
        try {
            List<User> allUsers = userRepository.findAll();
            return allUsers.stream()
                    .filter(user -> preferenceService.isChannelEnabled(user.getId(), channel))
                    .count();
        } catch (Exception e) {
            log.error("Error calculating enabled count for channel: {}", channel, e);
            return 0L;
        }
    }

    /**
     * REST endpoint to update notification preference
     * PUT /api/v2/notifications/preferences/channel?userId={id}&channel={channel}&enabled={true/false}
     */
    @PutMapping("/preferences/channel")
    @ResponseBody
    public Map<String, Object> updateNotificationPreference(
            @RequestParam Long userId,
            @RequestParam String channel,
            @RequestParam boolean enabled,
            HttpSession session
    ) {
        try {
            // Check authentication
            if (!isAuthenticated(session)) {
                return Map.of(
                        "success", false,
                        "message", "Unauthorized access"
                );
            }

            // Validate user exists
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return Map.of(
                        "success", false,
                        "message", "User not found with ID: " + userId
                );
            }

            // Parse channel
            NotificationChannel notificationChannel;
            try {
                notificationChannel = NotificationChannel.valueOf(channel.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Map.of(
                        "success", false,
                        "message", "Invalid channel: " + channel
                );
            }

            // Update preference
            preferenceService.updatePreference(userId, notificationChannel, enabled);

            log.info("Admin updated notification preference - User: {}, Channel: {}, Enabled: {}",
                    userId, channel, enabled);

            return Map.of(
                    "success", true,
                    "message", channel + " notification " + (enabled ? "enabled" : "disabled") + " successfully",
                    "userId", userId,
                    "channel", channel,
                    "enabled", enabled
            );

        } catch (Exception e) {
            log.error("Error updating notification preference", e);
            return Map.of(
                    "success", false,
                    "message", "Error updating preference: " + e.getMessage()
            );
        }
    }

    /**
     * REST endpoint to get user preferences
     * GET /api/v2/notifications/preferences/{userId}
     */
    @GetMapping("/preferences/{userId}")
    @ResponseBody
    public Map<String, Object> getUserPreferences(
            @PathVariable Long userId,
            HttpSession session
    ) {
        try {
            // Check authentication
            if (!isAuthenticated(session)) {
                return Map.of(
                        "success", false,
                        "message", "Unauthorized access"
                );
            }

            // Validate user exists
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return Map.of(
                        "success", false,
                        "message", "User not found with ID: " + userId
                );
            }

            Map<NotificationChannel, Boolean> preferences = preferenceService.getUserPreferencesMap(userId);

            return Map.of(
                    "success", true,
                    "userId", userId,
                    "preferences", preferences
            );

        } catch (Exception e) {
            log.error("Error fetching user preferences", e);
            return Map.of(
                    "success", false,
                    "message", "Error fetching preferences: " + e.getMessage()
            );
        }
    }

    /**
     * REST endpoint to bulk update preferences
     * PUT /api/v2/notifications/preferences/bulk
     */
    @PutMapping("/preferences/bulk")
    @ResponseBody
    public Map<String, Object> bulkUpdatePreferences(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session
    ) {
        try {
            // Check authentication
            if (!isAuthenticated(session)) {
                return Map.of(
                        "success", false,
                        "message", "Unauthorized access"
                );
            }

            Long userId = Long.valueOf(requestBody.get("userId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Boolean> preferences = (Map<String, Boolean>) requestBody.get("preferences");

            // Validate user exists
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return Map.of(
                        "success", false,
                        "message", "User not found with ID: " + userId
                );
            }

            // Convert String keys to NotificationChannel
            Map<NotificationChannel, Boolean> channelPreferences = new EnumMap<>(NotificationChannel.class);
            for (Map.Entry<String, Boolean> entry : preferences.entrySet()) {
                try {
                    NotificationChannel channel = NotificationChannel.valueOf(entry.getKey().toUpperCase());
                    channelPreferences.put(channel, entry.getValue());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid channel in bulk update: {}", entry.getKey());
                }
            }

            preferenceService.updatePreferences(userId, channelPreferences);

            log.info("Admin bulk updated notification preferences for user: {}", userId);

            return Map.of(
                    "success", true,
                    "message", "Preferences updated successfully",
                    "userId", userId
            );

        } catch (Exception e) {
            log.error("Error bulk updating preferences", e);
            return Map.of(
                    "success", false,
                    "message", "Error updating preferences: " + e.getMessage()
            );
        }
    }

    /**
     * REST endpoint to get statistics
     * GET /api/v2/notifications/stats
     */
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getNotificationStats(HttpSession session) {
        try {
            // Check authentication
            if (!isAuthenticated(session)) {
                return Map.of(
                        "success", false,
                        "message", "Unauthorized access"
                );
            }

            long totalUsers = userRepository.count();
            long emailEnabled = calculateEnabledCount(NotificationChannel.EMAIL);
            long smsEnabled = calculateEnabledCount(NotificationChannel.SMS);
            long pushEnabled = calculateEnabledCount(NotificationChannel.PUSH);
            long inAppEnabled = calculateEnabledCount(NotificationChannel.IN_APP);

            return Map.of(
                    "success", true,
                    "totalUsers", totalUsers,
                    "emailEnabled", emailEnabled,
                    "smsEnabled", smsEnabled,
                    "pushEnabled", pushEnabled,
                    "inAppEnabled", inAppEnabled
            );

        } catch (Exception e) {
            log.error("Error fetching notification stats", e);
            return Map.of(
                    "success", false,
                    "message", "Error fetching stats: " + e.getMessage()
            );
        }
    }
}