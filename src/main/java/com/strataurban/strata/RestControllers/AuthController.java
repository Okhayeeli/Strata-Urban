package com.strataurban.strata.RestControllers;

import com.strataurban.strata.Entities.Providers.Supplier;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.DTOs.*;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Security.jwtConfigs.JwtService;
import com.strataurban.strata.Services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    private SupplierService supplierService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return ResponseEntity.ok(registeredUser);
    }

//
//    @PostMapping("/register")
//    public ResponseEntity<?> registerSupplier(@RequestBody User supplier){
//        supplier.setRoles(EnumRoles.SUPPLIER);
//        User registeredUser = userService.registerUser(supplier);
//
//        return ResponseEntity.ok(registeredUser);
//    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("username", user.getUsername());
        response.put("roles", user.getAuthorities());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            String token = refreshToken.substring(7);
            if (jwtService.validateToken(token)) {
                String username = jwtService.getUsernameFromToken(token);
                User user = (User) userService.loadUserByUsername(username);

                String newAccessToken = jwtService.generateAccessToken(user);

                Map<String, Object> response = new HashMap<>();
                response.put("accessToken", newAccessToken);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password successfully reset"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}