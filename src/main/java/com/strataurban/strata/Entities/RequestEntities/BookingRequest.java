package com.strataurban.strata.Entities.RequestEntities;

import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String offerIds;

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

    // Basic Trip Details
    @Column
    private LocalDateTime takeoffDateTime;
    @Column
    private LocalDateTime createdDate;
    @Column
    private LocalDateTime pickupDateTime;
    @Column
    private LocalDateTime dropOffDateTime;
    @Column
    private Boolean hasMultipleStops;
    @Column
    private Boolean isReturnTrip;
    @Column
    private Boolean timingFlexible;
    @Column
    private String additionalNotes;
    @Column
    private String additionalStops; // (Intended an array of Strings)

    // Passenger Details (when transport_category is Passenger)
    @Column
    private Boolean isPassenger;
    @Column
    private String eventType; // Kind of event (from PDF)
    @Column
    private Integer numberOfPassengers; // MANDATORY for Passenger: Total number of people traveling (from PDF)
    @Column
    private Boolean luggageNeeded; // Is space for luggage required? (from PDF)
    @Column
    private String luggageDetails; // Details on luggage type/amount (from PDF)
    @Column
    private String vehiclePreferenceType; // Preferred vehicle type (from PDF)

//    @ElementCollection
//    private List<String> amenities; // Specific passenger amenities (from PDF)
    @Column
    private String otherRequests; // Other specific passenger requests (from PDF)
    @Column
    private Boolean waitOnSite; // Does the driver need to wait on-site?
    @Column
    private String additionalStopsDetails; // Further details if multiple stops (Conditional, if wait_on_site or has_multiple_stops is true)
    @Column
    private String budgetRange; // Budget expectations for passenger transport (from PDF)
    @Column
    private String preferredFeatures; // (Intended array of Strings)

    // Cargo Details (when transport_category is Cargo)
    @Column
    private Boolean isCargo;
    @Column
    private String supplyType; // Type of goods being transported
    @Column
    private Double estimatedWeightKg; // Estimated weight in kilograms (MANDATORY for Cargo)
    @Column
    private Double volumeCubicMeters; // Estimated volume in cubic meters (Optional, but helpful for Cargo)
    @Column
    private String packageSize; // Description of package size, e.g., "Small", "Medium", "Large", "Pallet" (MANDATORY for Cargo)
    @Column
    private String materialType; // Type of material, if relevant
    @Column
    private Boolean needsFragileHandling; // Requires special fragile handling?
    @Column
    private Double estimatedValue; // Estimated value of the cargo (Optional)
    @Column
    private String handlingInstruction; // Specific instructions for handling
    @Column
    private Boolean offloadingHelpRequired; // Is help needed with offloading?

    // Medical Details (when transport_category is Medical)
    @Column
    private Boolean isMedical;
    @Column
    private String medicalItemType; // Type of medical item or patient transport (MANDATORY for Medical)
    @Column
    private Boolean refrigerationRequiredMedical; // Does it require refrigeration?
    @Column
    private Boolean needsFragileHandlingMedical; // Inherited from cargo, also relevant here

    // Furniture Details (when transport_category is Furniture)
    @Column
    private Boolean isFurniture;
    @Column
    private String furnitureType; // Type of furniture (MANDATORY for Furniture)
    @Column
    private Boolean requiresDisassembly; // Does the furniture need to be disassembled?
    @Column
    private String handlingInstructionFurniture; // Inherited from cargo, also relevant here
    @Column
    private Boolean offloadingHelpRequiredFurniture; // Inherited from cargo, also relevant here

    // Food Details (when transport_category is Food)
    @Column
    private Boolean isFood;
    @Column
    private String foodType; // Type of food (MANDATORY for Food)
    @Column
    private Boolean requiresHotBox; // Does it require a hot box?
    @Column
    private Boolean refrigerationRequiredFood; // Also relevant for cold food
    @Column
    private String dietaryRestriction; // Any relevant dietary restrictions (if prepared food)
    @Column
    private Double estimatedWeightKgFood; // Estimated weight of food
    @Column
    private String packageSizeFood; // Description of packaging

    // Equipment Details (when transport_category is Equipment)
    @Column
    private Boolean isEquipment;
    @ElementCollection
    private List<String> equipmentList; // List of equipment items (MANDATORY for Equipment)
    @Column
    private Boolean setupRequired; // Is setup of the equipment required at the destination?


    private Boolean amenitiesNeeded;
    @Column
    private Boolean extraAmenitiesRequired; // Special requests indication
    private Boolean airConditioningRequired;
    private Boolean wiFiRequired;
    private Boolean wheelChairAccessibility;
    private Boolean powerOutletsRequired;
    private Boolean musicAndSoundSystemsRequired;
    private Boolean tintedWindowsRequired;
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
