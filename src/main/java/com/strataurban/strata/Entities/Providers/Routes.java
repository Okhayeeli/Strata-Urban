package com.strataurban.strata.Entities.Providers;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table
@Entity
public class Routes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String start;
    @Column
    private String end;
    @Column
    private BigDecimal price;
    @Column
    private String providerId;
    @Column
    private String state;
    @Column
    private String country;
    @Column
    private String city;
}
