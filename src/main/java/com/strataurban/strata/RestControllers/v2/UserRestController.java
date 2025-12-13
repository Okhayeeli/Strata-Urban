package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Notifications.UserContactService;
import com.strataurban.strata.Services.v2.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v2/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserContactService userContactService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve details of a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CUSTOMER_SERVICE, ADMIN, DEVELOPER, or the user themselves (principal.id == #id) can access this endpoint. CLIENT, PROVIDER, and others are restricted.")
    })
    @PreAuthorize("hasRole('CUSTOMER_SERVICE') or hasRole('ADMIN') or hasRole('DEVELOPER') or principal.id == #id")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserProfile(id));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CUSTOMER_SERVICE, ADMIN, DEVELOPER, or the user themselves (principal.id == #id) can access this endpoint. CLIENT, PROVIDER, and others are restricted.");
        }
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CUSTOMER_SERVICE, ADMIN, and DEVELOPER can access this endpoint. CLIENT, PROVIDER, and others are restricted.")
    })
    @PreAuthorize("hasRole('CUSTOMER_SERVICE') or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CUSTOMER_SERVICE, ADMIN, and DEVELOPER can access this endpoint. CLIENT, PROVIDER, and others are restricted.");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name or email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of matching users"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN and DEVELOPER can access this endpoint. CUSTOMER_SERVICE, CLIENT, PROVIDER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        try {
            return ResponseEntity.ok(userService.searchUsers(query));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN and DEVELOPER can access this endpoint. CUSTOMER_SERVICE, CLIENT, PROVIDER, and others are restricted.");
        }
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieve users with a specific role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users with role"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN and DEVELOPER can access this endpoint. CUSTOMER_SERVICE, CLIENT, PROVIDER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable EnumRoles role) {
        try {
            return ResponseEntity.ok(userService.getUsersByRole(role));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN and DEVELOPER can access this endpoint. CUSTOMER_SERVICE, CLIENT, PROVIDER, and others are restricted.");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN, DEVELOPER, or the user themselves (principal.id == #id) can access this endpoint. PROVIDER can update only if emailVerified and principal.id == #id. CUSTOMER_SERVICE, CLIENT, and others are restricted.")
    })
    @PreAuthorize("(hasRole('ADMIN') or hasRole('DEVELOPER') or (hasRole('PROVIDER') and principal.emailVerified and principal.id == #id)) or principal.id == #id")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN, DEVELOPER, or the user themselves (principal.id == #id) can access this endpoint. PROVIDER can update only if emailVerified and principal.id == #id. CUSTOMER_SERVICE, CLIENT, and others are restricted.");
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete user", description = "Delete a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN and DEVELOPER can delete a user. CUSTOMER_SERVICE, CLIENT, PROVIDER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<Void> deleteUser(@RequestBody RequestBodyIdDto request) {
        try {
            userService.deleteUser(request.getId());
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN and DEVELOPER can delete a user. CUSTOMER_SERVICE, CLIENT, PROVIDER, and others are restricted.");
        }
    }

    @GetMapping("/provider/{providerId}/clients")
    @Operation(summary = "Get clients in provider service area", description = "Retrieve clients within a provider's service area")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of clients"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only the PROVIDER themselves (principal.id == #providerId) can access this endpoint. ADMIN and DEVELOPER are allowed, but CUSTOMER_SERVICE, CLIENT, and others are restricted. DEVELOPER cannot create or operate PROVIDER-specific endpoints.")
    })
    @PreAuthorize("hasRole('PROVIDER') and principal.id == #providerId or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<User>> getClientsInProviderServiceArea(@PathVariable Long providerId) {
        try {
            List<User> clients = userService.getClientsInProviderServiceArea(providerId);
            return ResponseEntity.ok(clients);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only the PROVIDER themselves (principal.id == #providerId) can access this endpoint. ADMIN and DEVELOPER are allowed, but CUSTOMER_SERVICE, CLIENT, and others are restricted. DEVELOPER cannot create or operate PROVIDER-specific endpoints.");
        }
    }

    @PutMapping("/{id}/session-timeout")
    @Operation(summary = "Update session timeout", description = "Set a custom session timeout for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session timeout updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only the user themselves (principal.id == #id) can update their session timeout. ADMIN, DEVELOPER, CUSTOMER_SERVICE, PROVIDER, CLIENT, and others are restricted.")
    })
    @PreAuthorize("principal.id == #id")
    public ResponseEntity<Void> updateSessionTimeout(@PathVariable Long id, @RequestBody int timeoutMinutes) {
        try {
            userService.updateSessionTimeout(id, timeoutMinutes);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only the user themselves (principal.id == #id) can update their session timeout. ADMIN, DEVELOPER, CUSTOMER_SERVICE, PROVIDER, CLIENT, and others are restricted.");
        }
    }

    /**
     * Get user profile with notification readiness
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        // Your existing profile logic...

        UserProfileResponse response = new UserProfileResponse();
        // ... set user data

        // Add notification readiness info
        response.setCanReceiveEmail(userContactService.hasEmail(userId));
        response.setCanReceiveSms(userContactService.hasPhoneNumber(userId));
        response.setCanReceivePush(userContactService.hasDeviceToken(userId));

        return ResponseEntity.ok(response);
    }
}