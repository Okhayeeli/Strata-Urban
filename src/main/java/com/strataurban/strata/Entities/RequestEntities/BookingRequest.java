package com.strataurban.strata.Entities.RequestEntities;

import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking_request")
public class BookingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column
    private Long clientId;

    @Column
    private Long driverId;

    @Column
    private Long vehicleId;

    @Column
    private Long providerId;

    @Column
    @Enumerated(EnumType.STRING)
    private EnumPriority priority;

    @Column
    private Long routeId;

    @Column
    private LocalDateTime serviceDate;

    @Column
    private String pickUpLocation;

    @Column
    private String destination;

    @Column
    private String rateInformation;

    @Column
    private Long serviceId;

    // New address fields
    @Column
    private String pickupCountry;

    @Column
    private String pickupState;

    @Column
    private String pickupCity;

    @Column
    private String pickupStreet;

    @Column
    private String pickupLga;

    @Column
    private String pickupTown;

    @Column
    private String destinationCountry;

    @Column
    private String destinationState;

    @Column
    private String destinationCity;

    @Column
    private String destinationStreet;

    @Column
    private String destinationLga;

    @Column
    private String destinationTown;

    // New takeoff time and date
    @Column
    private LocalDateTime takeoffDateTime;

    // New created date
    @Column
    private LocalDateTime createdDate;

    // Transport-specific
    private Integer numberOfPeople;
    private String transportCategory; // e.g., Economy, VIP

    // Supplies-specific
    private String supplyType;
    private Double estimatedWeightKg;
    private String packageSize; // e.g., Small, Medium, Large

    // Gifts-specific
    private String giftCategory;
    private String giftMessage;
    private Boolean isWrapped;

    // Instruments-specific
    private String instrumentType;
    private Boolean needsFragileHandling;
    private Double estimatedValue;

    // Furniture-specific
    private String furnitureType;
    private Boolean requiresDisassembly;
    private String handlingInstruction;

    // Event Equipment-specific
    private String eventType;
    @ElementCollection
    private List<String> equipmentList;
    private Boolean setupRequired;

    // Food Delivery-specific
    private String foodType;
    private Boolean requiresHotBox;
    private String dietaryRestriction;

    // Construction Material-specific
    private String materialType;
    private Boolean offloadingHelpRequired;
    private Double volumeInCubicMeters;

    // Medical Transport-specific
    private String medicalItemType;
    private Boolean refrigerationRequired;
    private String urgencyLevel;

    // Common fields
    private LocalDateTime pickupDateTime;
    private LocalDateTime dropOffDateTime;
    private Boolean hasMultipleStops;
    private Boolean isReturnTrip;
    private String additionalNotes;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}