package com.strataurban.strata.Entities.Generics;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long clientId;
    private String description;
    private Date activityDate;
    private String activityBy;

    private Date departureDate;
    private Date arrivalDate;
    private BigDecimal duration;
    private BigDecimal price;
    private Boolean isClosed;
    private String transportCompany;
    private BigDecimal discount;
    private String comment;
    private String driverName;
    private String pickupLocation;
    private String dropOffLocation;
    private Double rating;
    private String type;
}
