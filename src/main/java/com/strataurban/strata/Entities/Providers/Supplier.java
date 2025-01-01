package com.strataurban.strata.Entities.Providers;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table
@Entity
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String supplierCode;
    @Column
    private String companyName;
    @Column
    private String phoneNumber;
    @Column
    private String address;
    @Column
    private String city;
    @Column
    private String state;
    @Column
    private String zipCode;
    @Column
    private String country;
    @Column
    private String preferredLanguage;
    @Column
    private String email;
    @Column
    private String password;
    @Column
    private Double rating;
    @Column
    private int numberOfRatings;
}
