package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.UpdateTokenRequest;
import com.strataurban.strata.Notifications.UserContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final UserContactService userContactService;

    /**
     * Update device token when Firebase refreshes it
     * Mobile app should call this endpoint when FCM token changes
     */
    @PutMapping("/token")
    public ResponseEntity<Map<String, String>> updateDeviceToken(
            @RequestParam Long userId,
            @RequestBody UpdateTokenRequest request) {

        log.info("Updating device token for user {}", userId);

        userContactService.updateDeviceToken(userId, request.getDeviceToken());

        return ResponseEntity.ok(Map.of(
                "message", "Device token updated successfully",
                "userId", userId.toString()
        ));
    }

    /**
     * Remove device token on logout
     */
    @DeleteMapping("/token")
    public ResponseEntity<Map<String, String>> removeDeviceToken(@RequestParam Long userId) {
        log.info("Removing device token for user {}", userId);

        userContactService.updateDeviceToken(userId, null);

        return ResponseEntity.ok(Map.of("message", "Device token removed"));
    }
}