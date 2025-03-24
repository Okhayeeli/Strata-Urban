package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a Booking Request")
public class BookingRequestDTO {

    @Schema(description = "Unique identifier of the booking request", example = "1")
    private Long id;

    @Schema(description = "Status of the trip", example = "PENDING")
    private TripStatus status;

    @Schema(description = "ID of the provider handling the booking", example = "2")
    private Long providerId;

    @Schema(description = "Priority of the booking", example = "HIGH")
    private EnumPriority priority;

    @Schema(description = "ID of the route associated with the booking", example = "3")
    private Long routeId;

    @Schema(description = "Date of the service", example = "2024-03-15")
    private LocalDateTime serviceDate;

    @Schema(description = "Pickup location", example = "Hotel Grandeur")
    private String pickUpLocation;

    @Schema(description = "Destination", example = "Convention Center")
    private String destination;

    @Schema(description = "Rate information for the booking", example = "$150")
    private String rateInformation;

    @Schema(description = "ID of the service associated with the booking", example = "4")
    private Long serviceId;

    // Transport-specific fields
    @Schema(description = "Number of people for transport", example = "15")
    private Integer numberOfPeople;

    @Schema(description = "Transport category", example = "VIP")
    private String transportCategory;

    // Supplies-specific fields
    @Schema(description = "Type of supply", example = "Electronics")
    private String supplyType;

    @Schema(description = "Estimated weight in kilograms", example = "50.5")
    private Double estimatedWeightKg;

    @Schema(description = "Package size", example = "Large")
    private String packageSize;

    // Gifts-specific fields
    @Schema(description = "Category of the gift", example = "Birthday")
    private String giftCategory;

    @Schema(description = "Gift message", example = "Happy Birthday!")
    private String giftMessage;

    @Schema(description = "Whether the gift is wrapped", example = "true")
    private Boolean isWrapped;

    // Instruments-specific fields
    @Schema(description = "Type of instrument", example = "Guitar")
    private String instrumentType;

    @Schema(description = "Whether the instrument needs fragile handling", example = "true")
    private Boolean needsFragileHandling;

    @Schema(description = "Estimated value of the instrument", example = "1000.0")
    private Double estimatedValue;

    // Furniture-specific fields
    @Schema(description = "Type of furniture", example = "Sofa")
    private String furnitureType;

    @Schema(description = "Whether the furniture requires disassembly", example = "false")
    private Boolean requiresDisassembly;

    @Schema(description = "Handling instructions for the furniture", example = "Handle with care")
    private String handlingInstruction;

    // Event Equipment-specific fields
    @Schema(description = "Type of event", example = "Conference")
    private String eventType;

    @Schema(description = "List of equipment for the event", example = "[\"Projector\", \"Speakers\"]")
    private List<String> equipmentList;

    @Schema(description = "Whether setup is required for the event", example = "true")
    private Boolean setupRequired;

    // Food Delivery-specific fields
    @Schema(description = "Type of food", example = "Pizza")
    private String foodType;

    @Schema(description = "Whether a hot box is required", example = "true")
    private Boolean requiresHotBox;

    @Schema(description = "Dietary restrictions", example = "Gluten-free")
    private String dietaryRestriction;

    // Construction Material-specific fields
    @Schema(description = "Type of material", example = "Cement")
    private String materialType;

    @Schema(description = "Whether offloading help is required", example = "true")
    private Boolean offloadingHelpRequired;

    @Schema(description = "Volume in cubic meters", example = "10.5")
    private Double volumeInCubicMeters;

    // Medical Transport-specific fields
    @Schema(description = "Type of medical item", example = "Vaccines")
    private String medicalItemType;

    @Schema(description = "Whether refrigeration is required", example = "true")
    private Boolean refrigerationRequired;

    @Schema(description = "Urgency level of the medical transport", example = "High")
    private String urgencyLevel;

    // Common fields
    @Schema(description = "Pickup date and time", example = "2024-03-15T09:00:00")
    private LocalDateTime pickupDateTime;

    @Schema(description = "Drop-off date and time", example = "2024-03-15T11:00:00")
    private LocalDateTime dropOffDateTime;

    @Schema(description = "Whether the trip has multiple stops", example = "false")
    private Boolean hasMultipleStops;

    @Schema(description = "Whether the trip is a return trip", example = "false")
    private Boolean isReturnTrip;

    @Schema(description = "Additional notes for the booking", example = "Please arrive 10 minutes early")
    private String additionalNotes;
}