package com.strataurban.strata.Entities;

import com.strataurban.strata.Enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Table(name = "user")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class User implements UserDetails {

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

//    @Email(message = "Invalid email format")
//    @NotBlank(message = "Email is required")
    @Column
    private String email;

//    @NotBlank(message = "Username is required")
//    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    @Column
    private String username;

//    @NotBlank(message = "Password is required")
//    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
//            message = "Password must be at least 8 characters long and contain at least one digit, " +
//                    "one uppercase letter, one lowercase letter, and one special character")
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

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;
    private String imageUrl;

    @Column
    private boolean enabled = true;

    @Column
    private boolean accountNonExpired = true;

    @Column
    private boolean accountNonLocked = true;

    @Column
    private boolean credentialsNonExpired = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roles.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
