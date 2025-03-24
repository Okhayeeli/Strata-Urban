package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.Passengers.Client;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Services.v2.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/users")
@Tag(name = "User Management", description = "APIs for managing users (Clients, Providers, Admins, etc.)")
public class UserRestController {

    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/client")
    @Operation(summary = "Register a new client", description = "Registers a new client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid client data")
    })
    public ResponseEntity<UserDTO> registerClient(@RequestBody ClientRegistrationRequest request) {
        Client client = userService.registerClient(request);
        return ResponseEntity.ok(mapToDTO(client));
    }

    @PostMapping("/register/provider")
    @Operation(summary = "Register a new provider", description = "Registers a new provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid provider data")
    })
    public ResponseEntity<UserDTO> registerProvider(@RequestBody ProviderRegistrationRequest request) {
        Provider provider = userService.registerProvider(request);
        return ResponseEntity.ok(mapToDTO(provider));
    }

    @PostMapping("/register/internal")
    @Operation(summary = "Register an internal user", description = "Registers a new internal user (Admin, Customer Service, Developer) - Admin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Internal user registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> registerInternalUser(@RequestBody AdminRegistrationRequest request) {
        User user = userService.registerInternalUser(request);
        return ResponseEntity.ok(mapToDTO(user));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String token = userService.login(loginRequest);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID", description = "Fetches the profile of a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapToDTO(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Updates the profile of a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(mapToDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user account", description = "Deletes a specific user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/verify-email")
    @Operation(summary = "Verify email", description = "Verifies a user's email using a token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> verifyEmail(
            @PathVariable Long id,
            @RequestParam String token) {
        userService.verifyEmail(id, token);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/change-password")
    @Operation(summary = "Change password", description = "Changes the password of a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Fetches all users (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Searches users by name or email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        List<UserDTO> userDTOs = users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Fetches all users with a specific role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role")
    })
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable EnumRoles role) {
        List<User> users = userService.getUsersByRole(role);
        List<UserDTO> userDTOs = users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getTitle(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                user.isEmailVerified(),
                user.getEmail(),
                user.getUsername(),
                user.getPhone(),
                user.getPhone2(),
                user.getAddress(),
                user.getPreferredLanguage(),
                user.getCity(),
                user.getState(),
                user.getCountry(),
                user.getRoles(),
                user.getImageUrl()
        );
    }
}