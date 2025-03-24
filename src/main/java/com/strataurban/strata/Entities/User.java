package com.strataurban.strata.Entities;

import com.strataurban.strata.Enums.*;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Table(name = "user")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class User{

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

}
