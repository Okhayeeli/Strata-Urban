package com.strataurban.strata.Entities;

import com.strataurban.strata.Enums.ProviderRole;
import jakarta.persistence.*;


import com.strataurban.strata.Enums.EnumRoles;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String firstName;

    @Column
    private String middleName;

    @Column
    private String lastName;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column
    private String email;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String phone;

    @Column
    private String phone2;

    @Column
    private String address;

    @Column
    private String preferredLanguage;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String country;

    @Column
    @Enumerated(EnumType.STRING)
    private EnumRoles roles;

    @Column
    private String providerId;

    @Column
    private String imageUrl;


    @Column(name = "failed_login_attempts", columnDefinition = "int default 0")
    private int failedLoginAttempts;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @Column(name = "preferred_session_timeout_minutes", columnDefinition = "int default 30")
    private int preferredSessionTimeoutMinutes;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "current_refresh_token_jti")
    private String currentRefreshTokenJti;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_role", columnDefinition = "varchar(255) default 'NONE'")
    private ProviderRole providerRole;

}