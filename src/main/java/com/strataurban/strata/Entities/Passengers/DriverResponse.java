package com.strataurban.strata.Entities.Passengers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Driver Response
 * Used to return driver information in API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String imageUrl;
    
    // Driver-specific fields
    private String licenseNumber;
    private String driverStatus;
    private Double rating;
    private Integer numberOfRatings;
    private Integer completedTrips;
    private Integer totalTrips;
    private Double completionRate;
    
    // Vehicle information
    private String vehicleType;
    private String vehicleMake;
    private String vehicleModel;
    private String vehiclePlateNumber;
    private String vehicleColor;
    
    // Current location
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentLocationName;
    
    // Availability
    private boolean isAvailable;
    private boolean isOnTrip;
    private Long currentTripId;
    
    // Provider information
    private String providerId;
    private String providerName;
    
    // Additional info
    private String approvalStatus;
    private boolean licenseVerified;
    private boolean insuranceVerified;
}
