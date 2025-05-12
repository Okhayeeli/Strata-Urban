package com.strataurban.strata.Security;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.ServiceImpls.v2.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionTimeoutFilter extends OncePerRequestFilter {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserServiceImpl userServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null) {
            User user = userServiceImpl.findUserByUsername(username);
            userService.updateLastActivity(user.getId());
            if (userService.isSessionExpired(user.getId())) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired due to inactivity");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}