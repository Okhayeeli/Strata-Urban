package com.strataurban.strata.DTOs.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response for a booking request")
public class BookingRequestResponseDTO {

    @Schema(description = "Details of the booking request")
    private BookingRequestDetails bookingRequest;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Booking request details")
    public static class BookingRequestDetails {
        @Schema(description = "Request details")
        private RequestDetails requestDetails;

        @Schema(description = "Location details")
        private Locations locations;

        @Schema(description = "Contact information")
        private ContactInformation contactInformation;

        @Schema(description = "Category-specific details")
        private CategorySpecificDetails categorySpecificDetails;

        @Schema(description = "Route")
        private String route;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request details")
    public static class RequestDetails {

        @Schema(description = "Id of the Booking Request")
        private Long id;

        @Schema(description = "Type of transport needed", example = "Passenger")
        private String transportCategory;

        @Schema(description = "Urgency level of the request", example = "Standard")
        private String urgencyLevel;

        @Schema(description = "Primary date for the service", example = "2025-05-15")
        private LocalDateTime serviceDate;

        @Schema(description = "Date and time for pick-up", example = "2025-05-15T09:00:00")
        private LocalDateTime pickUpDateTime;

        @Schema(description = "Date and time for drop-off", example = "2025-05-15T12:00:00", required = false)
        private LocalDateTime dropOffDateTime;

        @Schema(description = "Indicates if the requested times are flexible", example = "true")
        private Boolean timingFlexible;

        @Schema(description = "Indicates if this is a round trip", example = "false")
        private Boolean isReturnTrip;

        @Schema(description = "Additional notes for the request", example = "Need a comfortable ride")
        private String additionalNotes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Location details")
    public static class Locations {
        @Schema(description = "Pick-up location address", example = "123 Main St")
        private String pickUpLocation;

        @Schema(description = "Destination address", example = "456 Elm St")
        private String destination;

        @Schema(description = "Indicates if there are multiple stops", example = "false")
        private Boolean hasMultipleStops;

        @Schema(description = "List of additional stop addresses", example = "[\"789 Oak St\"]", required = false)
        private List<String> additionalStops;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Contact information")
    public static class ContactInformation {
        @Schema(description = "Name of the requestor", example = "John Doe")
        private String name;

        @Schema(description = "Phone number of the requestor", example = "555-1234")
        private String phoneNumber;

        @Schema(description = "Email address of the requestor", example = "john@example.com")
        private String emailAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Category-specific details")
    public static class CategorySpecificDetails {
        @Schema(description = "Passenger transport details", required = false)
        private PassengerDetails passengerDetails;

        @Schema(description = "Cargo transport details", required = false)
        private CargoDetails cargoDetails;

        @Schema(description = "Medical transport details", required = false)
        private MedicalDetails medicalDetails;

        @Schema(description = "Furniture transport details", required = false)
        private FurnitureDetails furnitureDetails;

        @Schema(description = "Food transport details", required = false)
        private FoodDetails foodDetails;

        @Schema(description = "Equipment transport details", required = false)
        private EquipmentDetails equipmentDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Passenger transport details")
    public static class PassengerDetails {
        @Schema(description = "Type of event", example = "Business")
        private String eventType;

        @Schema(description = "Total number of passengers", example = "4")
        private Integer numberOfPassengers;

        @Schema(description = "Indicates if space for luggage is required", example = "true")
        private Boolean luggageNeeded;

        @Schema(description = "Details on luggage type/amount", example = "2 suitcases")
        private String luggageDetails;

        @Schema(description = "Preferred vehicle type", example = "Sedan")
        private String vehiclePreferenceType;

        @Schema(description = "Special requests or amenities")
        private SpecialRequests specialRequests;

        @Schema(description = "Waiting time or stop details")
        private WaitingTimeDetails waitingTimeDetails;

        @Schema(description = "Budget expectations")
        private BudgetAndPricing budgetAndPricing;

        @Schema(description = "Quote comparison preferences")
        private QuoteComparisonPreferences quoteComparisonPreferences;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Special requests or amenities")
    public static class SpecialRequests {
        @Schema(description = "List of amenities", example = "[\"Air conditioning\", \"Wi-Fi\"]")
        private List<String> amenities;

        @Schema(description = "Other specific requests", example = "Quiet ride")
        private String otherRequests;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Waiting time or stop details")
    public static class WaitingTimeDetails {
        @Schema(description = "Indicates if driver needs to wait on-site", example = "false")
        private Boolean waitOnSite;

        @Schema(description = "Details for additional stops", example = "Stop for 30 minutes", required = false)
        private String additionalStopsDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Budget expectations")
    public static class BudgetAndPricing {
        @Schema(description = "Budget range", example = "Mid-range")
        private String budgetRange;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Quote comparison preferences")
    public static class QuoteComparisonPreferences {
        @Schema(description = "Preferred features for quotes", example = "[\"Best price\", \"Most amenities\"]")
        private List<String> preferredFeatures;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Cargo transport details")
    public static class CargoDetails {
        @Schema(description = "Type of goods", example = "Electronics")
        private String supplyType;

        @Schema(description = "Estimated weight in kilograms", example = "100.5")
        private Double estimatedWeightKg;

        @Schema(description = "Estimated volume in cubic meters", example = "2.5", required = false)
        private Double volumeCubicMeters;

        @Schema(description = "Package size", example = "Large")
        private String packageSize;

        @Schema(description = "Type of material", example = "Plastic", required = false)
        private String materialType;

        @Schema(description = "Requires special fragile handling", example = "true")
        private Boolean needsFragileHandling;

        @Schema(description = "Estimated value of cargo", example = "5000.0", required = false)
        private Double estimatedValue;

        @Schema(description = "Handling instructions", example = "Handle with care")
        private String handlingInstruction;

        @Schema(description = "Help needed with offloading", example = "false")
        private Boolean offloadingHelpRequired;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Medical transport details")
    public static class MedicalDetails {
        @Schema(description = "Type of medical item or patient", example = "Medical supplies")
        private String medicalItemType;

        @Schema(description = "Requires refrigeration", example = "true")
        private Boolean refrigerationRequired;

        @Schema(description = "Requires special fragile handling", example = "false")
        private Boolean needsFragileHandling;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Furniture transport details")
    public static class FurnitureDetails {
        @Schema(description = "Type of furniture", example = "Sofa")
        private String furnitureType;

        @Schema(description = "Requires disassembly", example = "true")
        private Boolean requiresDisassembly;

        @Schema(description = "Handling instructions", example = "Avoid scratches")
        private String handlingInstruction;

        @Schema(description = "Help needed with offloading", example = "true")
        private Boolean offloadingHelpRequired;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Food transport details")
    public static class FoodDetails {
        @Schema(description = "Type of food", example = "Perishable")
        private String foodType;

        @Schema(description = "Requires hot box", example = "false")
        private Boolean requiresHotBox;

        @Schema(description = "Requires refrigeration", example = "true")
        private Boolean refrigerationRequired;

        @Schema(description = "Dietary restrictions", example = "Gluten-free", required = false)
        private String dietaryRestriction;

        @Schema(description = "Estimated weight in kilograms", example = "50.0")
        private Double estimatedWeightKg;

        @Schema(description = "Package size", example = "Medium")
        private String packageSize;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Equipment transport details")
    public static class EquipmentDetails {
        @Schema(description = "List of equipment items", example = "[\"Projector\", \"Speakers\"]")
        private List<String> equipmentList;

        @Schema(description = "Requires setup at destination", example = "true")
        private Boolean setupRequired;
    }
}