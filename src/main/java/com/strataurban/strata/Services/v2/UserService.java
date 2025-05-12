package com.strataurban.strata.Services.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.Passengers.Client;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import java.util.List;

public interface UserService extends UserDetailsService {

    // Register a Client
    Client registerClient(ClientRegistrationRequest request);

    // Register a Provider
    Provider registerProvider(ProviderRegistrationRequest request);

    // Register an Admin or other internal roles (Admin only)
    User registerInternalUser(AdminRegistrationRequest request);

    // User login
//    String login(LoginRequest loginRequest);

    // Get user profile by ID
    User getUserById(Long id);

    // Update user profile
    User updateUser(Long id, User user);

    // Delete user account
    void deleteUser(Long id);

    // Verify email
    void verifyEmail(Long id, String token);

    // Change password
    void changePassword(Long id, ChangePasswordRequest request);

    // Get all users
    List<User> getAllUsers();

    // Search users by criteria
    List<User> searchUsers(String query);

    // Get users by role
    List<User> getUsersByRole(EnumRoles role);

    // User login
    LoginResponse login(LoginRequest loginRequest);

    //User logout
    void logout(String refreshToken);

    UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException;

    void incrementFailedLoginAttempts(User user);

    void resetFailedLoginAttempts(User user);

    List<User> getClientsInProviderServiceArea(Long providerId);

    void updateSessionTimeout(Long userId, int timeoutMinutes);
}
