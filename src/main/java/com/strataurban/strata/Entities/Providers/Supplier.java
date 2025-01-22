package com.strataurban.strata.Entities.Providers;

import com.strataurban.strata.Entities.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "suppliers")
@Entity
public class Supplier extends User {

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
