package com.strataurban.strata.Entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

    public CustomUserDetails(User user) {
        logger.debug("Constructing CustomUserDetails for user: {}", user.getUsername());
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRoles() != null ? user.getRoles().name() : null;
        this.emailVerified = user.isEmailVerified();
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        logger.debug("Checking isAccountNonLocked");
        return true;
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