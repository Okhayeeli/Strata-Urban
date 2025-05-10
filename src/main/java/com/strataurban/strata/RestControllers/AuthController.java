package com.strataurban.strata.RestControllers;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Repositories.v2.BlacklistedTokenRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.Security.jwtConfigs.JwtUtil;
import com.strataurban.strata.Services.v2.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v2/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/signup/client")
    @Operation(summary = "Register a new client", description = "Creates a new client user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration request")
    })
    public ResponseEntity<User> registerClient(@Valid @RequestBody ClientRegistrationRequest request) {
        return ResponseEntity.ok(userService.registerClient(request));
    }

    @PostMapping("/signup/provider")
    @Operation(summary = "Register a new provider", description = "Creates a new provider user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration request")
    })
    public ResponseEntity<User> registerProvider(@Valid @RequestBody ProviderRegistrationRequest request) {
        return ResponseEntity.ok(userService.registerProvider(request));
    }

    @PostMapping("/signup/internal")
    @Operation(summary = "Register an internal user", description = "Creates a new internal user (ADMIN, CUSTOMER_SERVICE, DEVELOPER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Internal user registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration request")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> registerInternalUser(@Valid @RequestBody AdminRegistrationRequest request) {
        return ResponseEntity.ok(userService.registerInternalUser(request));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    })
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequestDTO request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate a new access token using a refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        if (jwtUtil.validateToken(refreshToken) && blacklistedTokenRepository.findByJti(jwtUtil.getJtiFromToken(refreshToken)).isEmpty()) {
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String newAccessToken = jwtUtil.generateAccessToken(username, user.getRoles().name());
            LoginResponse response = new LoginResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(refreshToken);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }
}