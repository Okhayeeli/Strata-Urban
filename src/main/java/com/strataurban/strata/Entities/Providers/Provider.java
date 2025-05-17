package com.strataurban.strata.Entities.Providers;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.LogisticsServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@Table(name = "provider")
@Entity
public class Provider extends User {

    @Column
    private String companyLogoUrl;
    @Column
    private String primaryContactPosition;
    @Column
    private String primaryContactDepartment;
    @Column
    private String companyBannerUrl;
    @Column
    private String supplierCode;
    @Column
    private String companyName;
    @Column
    private String companyAddress;
    @Column
    private String companyBusinessType;
    @Column
    private String companyBusinessPhone;
    @Column
    private String companyBusinessWebsite;
    @Column
    private String companyBusinessEmail;
    @Column
    private String companyRegistrationNumber;
    @Column
    private String description;
    @Column
    private String zipCode;
    @Column
    private Double rating;
    @Column
    private int numberOfRatings;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<LogisticsServiceType> serviceTypes;
    @Column
    private String serviceAreas;
    @Column
    private Integer transportCount;
}