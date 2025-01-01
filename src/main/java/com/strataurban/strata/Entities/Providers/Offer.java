package com.strataurban.strata.Entities.Providers;

import com.strataurban.strata.Enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String offerId; // Unique identifier for the offer

    @Column
    private int numberOfVehicles; // Number of vehicles available in the offer

    @Column
    private BigDecimal pricePerUnit; // Price per vehicle unit (may vary depending on type)

    @Column
    private BigDecimal totalPrice; // Total price for the requested number of vehicles

    @Column
    private String vehicleType; // Type of vehicle (e.g., Sedan, SUV, etc.)

    @Column
    private String model; // Specific model of the vehicle (e.g., Corolla, Camry, etc.)

    @Column
    private String brand; // Brand of the vehicle (e.g., Toyota, Ford, etc.)

    @Column
    private int capacity; // Seating capacity of the vehicle

    @Column
    private String fuelType; // Type of fuel used by the vehicle (e.g., Petrol, Diesel, Electric)

    @Column
    private String transmissionType; // Type of transmission (e.g., Manual, Automatic)

    @Column
    private String vehicleColor; // Color of the vehicle (e.g., Red, Blue, Black)

    @Column
    private int vehicleAge; // Age of the vehicle in years

    @Column
    private String licensePlate; // License plate of the vehicle

    @Column
    private Long supplierId; // Unique identifier for the supplier

    @Column
    private Long clientId;

    @Column
    private String supplierName; // Name of the supplier providing the vehicles

    @Column
    private BigDecimal supplierRating; // Supplier's rating, if available (e.g., 4.5 out of 5)

    @Column
    @Enumerated(EnumType.STRING)
    private Status offerStatus; // Status of the offer (e.g., Pending, Accepted, Declined)

    @Column
    private LocalDateTime offerDate; // Date and time the offer was created

    @Column
    private LocalDateTime expiryDate; // Date and time when the offer expires

    @Column
    private BigDecimal discount; // Discount amount applied to the offer, if any

    @Column
    private String currency; // Currency for the price (e.g., USD, EUR, NGN)

    @Column
    private String pickupLocation; // Pickup location for the vehicle

    @Column
    private String dropOffLocation; // Drop-off location (optional, depending on service)

    @Column
    private LocalDateTime availabilityStart; // Start of the vehicle's availability period

    @Column
    private LocalDateTime availabilityEnd; // End of the vehicle's availability period

    @Column
    private String notes; // Additional notes from the supplier or about the offer
}
