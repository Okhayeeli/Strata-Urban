package com.strataurban.strata.Entities;

import com.strataurban.strata.Enums.*;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


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

//    @Enumerated(EnumType.STRING)
//    private Provider provider;

    private String providerId;
    private String imageUrl;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roles.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified; // Enable account only after email verification
    }
}
