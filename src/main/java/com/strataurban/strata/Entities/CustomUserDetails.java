package com.strataurban.strata.Entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomUserDetails implements org.springframework.security.core.userdetails.UserDetails {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetails.class);

    private final Long id;
    private final String username;
    private final String password;
    private final String role;
    private final boolean emailVerified;
    private final int failedLoginAttempts;
    private final LocalDateTime accountLockedUntil;
    private final LocalDateTime accountExpiryDate;

    public CustomUserDetails(User user) {
        logger.debug("Constructing CustomUserDetails for user: {}", user.getUsername());
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRoles() != null ? user.getRoles().name() : null;
        this.emailVerified = user.isEmailVerified();
        this.failedLoginAttempts = user.getFailedLoginAttempts();
        this.accountLockedUntil = user.getAccountLockedUntil();
        this.accountExpiryDate = user.getAccountExpiryDate();
    }

    public Long getId() {
        logger.debug("Getting id: {}", id);
        return id;
    }

    public String getRole() {
        logger.debug("Getting role: {}", role);
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        logger.debug("Getting authorities for role: {}", role);
        return role != null ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)) : Collections.emptyList();
    }

    @Override
    public String getPassword() {
        logger.debug("Getting password");
        return password;
    }

    @Override
    public String getUsername() {
        logger.debug("Getting username: {}", username);
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        logger.debug("Checking isAccountNonExpired");

        // If no expiry date is set, account is not expired
        if (accountExpiryDate == null) {
            return true;
        }

        // Check if current time is before expiry date
        boolean isNotExpired = LocalDateTime.now().isBefore(accountExpiryDate);
        logger.debug("Account expiry status - Expiry Date: {}, Is Not Expired: {}",
                accountExpiryDate, isNotExpired);

        return isNotExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        logger.debug("Checking isAccountNonLocked");
        if (accountLockedUntil == null) {
            return true;
        }
        boolean isLocked = accountLockedUntil.isAfter(LocalDateTime.now());
        logger.debug("Account locked status: {}", isLocked);
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        logger.debug("Checking isCredentialsNonExpired");
        return true;
    }

    @Override
    public boolean isEnabled() {
        logger.debug("Checking isEnabled: {}", emailVerified);
        return emailVerified;
    }

    @Override
    public String toString() {
        return "CustomUserDetails{id=" + id + ", username=" + username + ", role=" + role + "}";
    }
}