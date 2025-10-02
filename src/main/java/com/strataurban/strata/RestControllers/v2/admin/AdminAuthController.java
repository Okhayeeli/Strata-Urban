package com.strataurban.strata.RestControllers.v2.admin;

import com.strataurban.strata.DTOs.v2.LoginRequest;
import com.strataurban.strata.DTOs.v2.LoginResponse;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.Services.v2.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    /**
     * Display login page
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model,
            HttpSession session
    ) {
        // If already logged in, redirect to dashboard
        if (session.getAttribute("adminUser") != null) {
            return "redirect:/admin/dashboard";
        }

        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been successfully logged out.");
        }

        return "admin/login";
    }

    /**
     * Handle login form submission
     */
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam("usernameOrEmail") String usernameOrEmail,
            @RequestParam("password") String password,
            @RequestParam(value = "remember", required = false) String remember,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Create login request
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsernameOrEmail(usernameOrEmail);
            loginRequest.setPassword(password);

            // Authenticate user
            LoginResponse loginResponse = userService.login(loginRequest);

            // Extract user information from JWT token
            String accessToken = loginResponse.getAccessToken();

            // You can decode the JWT to get user info, or query the database
            // For now, let's query the database to get complete user info
            com.strataurban.strata.Entities.User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user has ADMIN role
            if (user.getRoles() == null || !user.getRoles().name().equalsIgnoreCase("ADMIN")) {
                redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
                return "redirect:/admin/login?error";
            }

            // Create a simple admin user object for session
            Map<String, Object> adminUser = new HashMap<>();
            adminUser.put("id", user.getId());
            adminUser.put("username", user.getUsername());
            adminUser.put("firstName", user.getFirstName());
            adminUser.put("lastName", user.getLastName());
            adminUser.put("email", user.getEmail());
            adminUser.put("role", user.getRoles().name());

            // Store user info in session
            session.setAttribute("adminUser", adminUser);
            session.setAttribute("accessToken", loginResponse.getAccessToken());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRoles().name());

            // Set session timeout (30 minutes)
            session.setMaxInactiveInterval(30 * 60);

            // If remember me is checked, extend session
            if (remember != null) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7 days
            }

            return "redirect:/admin/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid credentials. Please try again.");
            return "redirect:/admin/login?error";
        }
    }

    /**
     * Handle logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been successfully logged out.");
        return "redirect:/admin/login?logout";
    }

    /**
     * Display forgot password page
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "admin/forgot-password";
    }

    /**
     * Handle forgot password request
     */
    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Call your password reset service
            // passwordResetTokenService.requestPasswordReset(email);

            redirectAttributes.addFlashAttribute("message",
                    "If an account exists with this email, a password reset link has been sent.");
            return "redirect:/admin/forgot-password?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred. Please try again.");
            return "redirect:/admin/forgot-password?error";
        }
    }

    /**
     * Display reset password page
     */
    @GetMapping("/reset-password")
    public String showResetPasswordPage(
            @RequestParam("token") String token,
            Model model
    ) {
        model.addAttribute("token", token);
        return "admin/reset-password";
    }

    /**
     * Handle password reset
     */
    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
                return "redirect:/admin/reset-password?token=" + token + "&error";
            }

            // Call your password reset service
            // passwordResetTokenService.resetPassword(token, newPassword);

            redirectAttributes.addFlashAttribute("message",
                    "Password reset successful. Please login with your new password.");
            return "redirect:/admin/login?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid or expired reset token. Please request a new one.");
            return "redirect:/admin/reset-password?token=" + token + "&error";
        }
    }
}