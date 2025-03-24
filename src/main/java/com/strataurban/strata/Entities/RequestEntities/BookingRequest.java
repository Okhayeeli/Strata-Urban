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
@Table
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

    // ==== Transport-specific ====
    private Integer numberOfPeople;
    private String transportCategory; // e.g., Economy, VIP

    // ==== Supplies-specific ====
    private String supplyType;
    private Double estimatedWeightKg;
    private String packageSize; // e.g., Small, Medium, Large

    // ==== Gifts-specific ====
    private String giftCategory;
    private String giftMessage;
    private Boolean isWrapped;

    // ==== Instruments-specific ====
    private String instrumentType;
    private Boolean needsFragileHandling;
    private Double estimatedValue;


    // Furniture Specific
    private String furnitureType;
    private Boolean requiresDisassembly;
    private String handlingInstruction;

    // Event Equipment Specific
    private String eventType;
    @ElementCollection
    private List<String> equipmentList;
    private Boolean setupRequired;

    // Food Delivery Specific
    private String foodType;
    private Boolean requiresHotBox;
    private String dietaryRestriction;

    // Construction Material Specific
    private String materialType;
    private Boolean offloadingHelpRequired;
    private Double volumeInCubicMeters;

    // Medical Transport Specific
    private String medicalItemType;
    private Boolean refrigerationRequired;
    private String urgencyLevel;

    // ==== Common fields ====
    private LocalDateTime pickupDateTime;
    private LocalDateTime dropOffDateTime;
    private Boolean hasMultipleStops;
    private Boolean isReturnTrip;
    private String additionalNotes;
}
