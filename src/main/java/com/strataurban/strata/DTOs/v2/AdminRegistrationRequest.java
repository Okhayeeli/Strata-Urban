package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.EnumRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to register an Admin, Driver or other internal roles")
public class AdminRegistrationRequest {

    @Schema(description = "Title of the user", example = "Ms.")
    private String title;

    @Schema(description = "First name of the user", example = "Alice")
    private String firstName;

    @Schema(description = "Middle name of the user", example = "Marie")
    private String middleName;

    @Schema(description = "Last name of the user", example = "Johnson")
    private String lastName;

    @Schema(description = "Email address of the user", example = "alice.johnson@example.com")
    private String email;

    @Schema(description = "Username of the user", example = "alicej")
    private String username;

    @Schema(description = "Password of the user", example = "Password@123")
    private String password;

    @Schema(description = "Primary phone number of the user", example = "+1234567890")
    private String phone;

    @Schema(description = "Secondary phone number of the user", example = "+0987654321", required = false)
    private String phone2;

    @Schema(description = "Address of the user", example = "789 Admin Ave")
    private String address;

    @Schema(description = "Preferred language of the user", example = "English")
    private String preferredLanguage;

    @Schema(description = "City of the user", example = "Chicago")
    private String city;

    @Schema(description = "State of the user", example = "IL")
    private String state;

    @Schema(description = "Country of the user", example = "USA")
    private String country;

    @Schema(description = "URL of the user's image", example = "https://example.com/images/admin.jpg", required = false)
    private String imageUrl;

    @Schema(description = "Role of the user", example = "DRIVER")
    private EnumRoles role;

    @Schema(description = "Id of the Provider if a driver is being created", example = "123")
    private String providerId;

    /* ================= DRIVER METRICS ================= */

    @Schema(description = "Driver rating", example = "4.8")
    private Double rating;

    @Schema(description = "Number of ratings received", example = "120")
    private Integer numberOfRatings;

    @Schema(description = "Total trips assigned to the driver", example = "450")
    private Integer totalTrips;

    @Schema(description = "Total completed trips", example = "420")
    private Integer completedTrips;

    @Schema(description = "Total cancelled trips", example = "15")
    private Integer cancelledTrips;

    @Schema(description = "Total rejected trips", example = "15")
    private Integer rejectedTrips;

    /* ================= DRIVER STATUS ================= */

    @Schema(description = "Current driver status", example = "ONLINE")
    private String driverStatus;

    @Schema(description = "Whether the driver account is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether the driver is available for trips", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Whether the driver is currently on a trip", example = "false")
    private Boolean isOnTrip;

    @Schema(description = "Current trip ID if on a trip", example = "TRIP_12345", required = false)
    private String currentTripId;

    @Schema(description = "Last time the driver was active", example = "2025-01-30T14:20:00")
    private LocalDateTime lastActiveDate;

    @Schema(description = "Last completed trip date", example = "2025-01-29T18:45:00")
    private LocalDateTime lastTripCompletedDate;

    @Schema(description = "Total hours driver has been online", example = "320.5")
    private Double totalOnlineHours;

    @Schema(description = "Total online hours for today", example = "6.5")
    private Double onlineTodayHours;

    /* ================= BANKING & EARNINGS ================= */

    @Schema(description = "Bank account holder name", example = "John Doe")
    private String bankAccountName;

    @Schema(description = "Bank account number", example = "0123456789")
    private String bankAccountNumber;

    @Schema(description = "Bank name", example = "First Bank")
    private String bankName;

    @Schema(description = "Bank code", example = "011")
    private String bankCode;

    @Schema(description = "Tax identification number", example = "TIN123456")
    private String taxIdNumber;

    @Schema(description = "Total earnings", example = "150000.75")
    private Double totalEarnings;

    @Schema(description = "Pending earnings", example = "12000.50")
    private Double pendingEarnings;

    @Schema(description = "Withdrawn earnings", example = "138000.25")
    private Double withdrawnEarnings;

    /* ================= VEHICLE DETAILS ================= */

    @Schema(description = "Driver license number", example = "LIC123456")
    private String licenseNumber;

    @Schema(description = "Vehicle type", example = "Sedan")
    private String vehicleType;

    @Schema(description = "Vehicle make", example = "Toyota")
    private String vehicleMake;

    @Schema(description = "Vehicle model", example = "Camry")
    private String vehicleModel;

    @Schema(description = "Vehicle year", example = "2020")
    private Integer vehicleYear;

    @Schema(description = "Vehicle plate number", example = "ABC-123XY")
    private String vehiclePlateNumber;

    @Schema(description = "Vehicle color", example = "Black")
    private String vehicleColor;

    /* ================= EMERGENCY & PROFILE ================= */

    @Schema(description = "Emergency contact name", example = "Jane Doe")
    private String emergencyContactName;

    @Schema(description = "Emergency contact phone number", example = "+2348012345678")
    private String emergencyContactPhone;

    @Schema(description = "Languages spoken by the driver", example = "English, French")
    private String languagesSpoken;

    @Schema(description = "Years of driving experience", example = "5")
    private Integer yearsOfExperience;

    @Schema(description = "Special skills", example = "Defensive driving, First Aid")
    private String specialSkills;

    /* ================= APPROVAL & VERIFICATION ================= */

    @Schema(description = "Approval status", example = "APPROVED")
    private String approvalStatus;

    @Schema(description = "Document verification status", example = "VERIFIED")
    private String documentVerificationStatus;
}
