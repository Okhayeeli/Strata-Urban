package com.strataurban.strata.Entities.Providers;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Transport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long providerId;
    private String type; // Taxi or Bus
    private int capacity;
    private String description;
    private String plateNumber;
    private String brand;
    private String model;
    private String color;
    private String state;
    private String company;
    private Long routeId;
    private String status; // Available, Booked, etc.
}
