package com.strataurban.strata.Security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Allow access to login and password reset pages
        if (uri.startsWith("/admin/login") ||
                uri.startsWith("/admin/forgot-password") ||
                uri.startsWith("/admin/reset-password")) {
            return true;
        }

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            response.sendRedirect("/admin/login");
            return false;
        }

        // Check if user has ADMIN role
        Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
        String role = (String) adminUser.get("role");

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            response.sendRedirect("/admin/login?error");
            return false;
        }

        return true;
    }
}