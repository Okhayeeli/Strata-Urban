package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.Entities.Generics.Notification;
import com.strataurban.strata.Notifications.NotificationChannel;
import com.strataurban.strata.Notifications.NotificationFacade;
import com.strataurban.strata.Notifications.NotificationPreferenceDTO;
import com.strataurban.strata.Notifications.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for notification management
 * Simplified: Users choose CHANNELS (not channel+type combinations)
 */
@RestController
@RequestMapping("/api/v2/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationRestController {

    private final NotificationFacade notificationFacade;
    private final NotificationPreferenceService preferenceService;

    // ===== NOTIFICATION QUERIES =====

    /**
     * Get user's notifications (paginated)
     * GET /api/v2/notifications?userId=123
     */
    @GetMapping
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @RequestParam Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Fetching notifications for user {}", userId);
        Page<Notification> notifications = notificationFacade.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count
     * GET /api/v2/notifications/unread-count?userId=123
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam Long userId) {
        log.info("Fetching unread count for user {}", userId);
        long count = notificationFacade.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Mark notification as read
     * PUT /api/v2/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        log.info("Marking notification {} as read", id);
        notificationFacade.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    /**
     * Mark all notifications as read
     * PUT /api/v2/notifications/read-all?userId=123
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@RequestParam Long userId) {
        log.info("Marking all notifications as read for user {}", userId);
        notificationFacade.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    /**
     * Delete notification
     * DELETE /api/v2/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        log.info("Deleting notification {}", id);
        notificationFacade.deleteNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    // ===== PREFERENCE MANAGEMENT (SIMPLIFIED) =====

    /**
     * Get user's notification preferences
     * Returns: [
     *   {channel: "EMAIL", enabled: true},
     *   {channel: "SMS", enabled: false},
     *   {channel: "PUSH", enabled: false},
     *   {channel: "IN_APP", enabled: true}
     * ]
     * GET /api/v2/notifications/preferences?userId=123
     */
    @GetMapping("/preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> getUserPreferences(@RequestParam Long userId) {
        log.info("Fetching preferences for user {}", userId);
        List<NotificationPreferenceDTO> preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Get user's preferences as a simple map
     * Returns: {EMAIL: true, SMS: false, PUSH: false, IN_APP: true}
     * GET /api/v2/notifications/preferences/map?userId=123
     */
    @GetMapping("/preferences/map")
    public ResponseEntity<Map<NotificationChannel, Boolean>> getUserPreferencesMap(@RequestParam Long userId) {
        log.info("Fetching preferences map for user {}", userId);
        Map<NotificationChannel, Boolean> preferences = preferenceService.getUserPreferencesMap(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Get list of enabled channels only
     * Returns: ["EMAIL", "IN_APP"]
     * GET /api/v2/notifications/preferences/enabled?userId=123
     */
    @GetMapping("/preferences/enabled")
    public ResponseEntity<List<NotificationChannel>> getEnabledChannels(@RequestParam Long userId) {
        log.info("Fetching enabled channels for user {}", userId);
        List<NotificationChannel> channels = preferenceService.getEnabledChannels(userId);
        return ResponseEntity.ok(channels);
    }

    /**
     * Update a single channel preference
     * PUT /api/v2/notifications/preferences/channel
     * Body: {userId: 123, channel: "EMAIL", enabled: true}
     */
    @PutMapping("/preferences/channel")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationPreferenceDTO> updateChannelPreference(
            @RequestParam Long userId,
            @RequestParam NotificationChannel channel,
            @RequestParam boolean enabled) {

        log.info("Updating channel preference for user {}: {} = {}", userId, channel, enabled);

        NotificationPreferenceDTO updated = preferenceService.updatePreference(userId, channel, enabled);

        return ResponseEntity.ok(updated);
    }

    /**
     * Bulk update all channel preferences
     * PUT /api/v2/notifications/preferences/bulk
     * Body: {EMAIL: true, SMS: false, PUSH: true, IN_APP: true}
     */
    @PutMapping("/preferences/bulk")
    public ResponseEntity<List<NotificationPreferenceDTO>> bulkUpdatePreferences(
            @RequestParam Long userId,
            @RequestBody Map<NotificationChannel, Boolean> preferences) {

        log.info("Bulk updating preferences for user {}: {}", userId, preferences);
        List<NotificationPreferenceDTO> updated = preferenceService.updatePreferences(userId, preferences);
        return ResponseEntity.ok(updated);
    }

    /**
     * Enable a specific channel
     * POST /api/v2/notifications/preferences/enable?userId=123&channel=EMAIL
     */
    @PostMapping("/preferences/enable")
    public ResponseEntity<Map<String, String>> enableChannel(
            @RequestParam Long userId,
            @RequestParam NotificationChannel channel) {

        log.info("Enabling channel {} for user {}", channel, userId);
        preferenceService.enableChannel(userId, channel);
        return ResponseEntity.ok(Map.of(
                "message", channel + " notifications enabled",
                "info", "You will now receive ALL notification types via " + channel
        ));
    }

    /**
     * Disable a specific channel
     * POST /api/v2/notifications/preferences/disable?userId=123&channel=EMAIL
     */
    @PostMapping("/preferences/disable")
    public ResponseEntity<Map<String, String>> disableChannel(
            @RequestParam Long userId,
            @RequestParam NotificationChannel channel) {

        log.info("Disabling channel {} for user {}", channel, userId);
        preferenceService.disableChannel(userId, channel);
        return ResponseEntity.ok(Map.of(
                "message", channel + " notifications disabled",
                "info", "You will no longer receive notifications via " + channel
        ));
    }

    /**
     * Disable all notifications
     * POST /api/v2/notifications/preferences/disable-all?userId=123
     */
    @PostMapping("/preferences/disable-all")
    public ResponseEntity<Map<String, String>> disableAllNotifications(@RequestParam Long userId) {
        log.info("Disabling all notifications for user {}", userId);
        preferenceService.disableAllNotifications(userId);
        return ResponseEntity.ok(Map.of(
                "message", "All notifications disabled",
                "warning", "You will not receive any notifications. You can re-enable them anytime."
        ));
    }

    /**
     * Initialize default preferences for a new user
     * POST /api/v2/notifications/preferences/initialize?userId=123
     */
    @PostMapping("/preferences/initialize")
    public ResponseEntity<Map<String, String>> initializeDefaultPreferences(@RequestParam Long userId) {
        log.info("Initializing default preferences for user {}", userId);
        preferenceService.initializeDefaultPreferences(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Default preferences initialized",
                        "info", "IN_APP notifications enabled by default. Enable other channels in settings."
                ));
    }

    /**
     * Delete all preferences for a user
     * DELETE /api/v2/notifications/preferences?userId=123
     */
    @DeleteMapping("/preferences")
    public ResponseEntity<Map<String, String>> deleteUserPreferences(@RequestParam Long userId) {
        log.info("Deleting all preferences for user {}", userId);
        preferenceService.deleteUserPreferences(userId);
        return ResponseEntity.ok(Map.of("message", "All preferences deleted"));
    }
}