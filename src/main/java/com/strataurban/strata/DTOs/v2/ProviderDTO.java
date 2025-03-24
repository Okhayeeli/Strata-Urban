package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Enums.LogisticsServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a Provider")
public class ProviderDTO {

    @Schema(description = "Unique identifier of the provider", example = "1")
    private Long id;

    @Schema(description = "Title of the provider", example = "Mr.")
    private String title;

    @Schema(description = "First name of the provider", example = "John")
    private String firstName;

    @Schema(description = "Middle name of the provider", example = "Michael")
    private String middleName;

    @Schema(description = "Last name of the provider", example = "Doe")
    private String lastName;

    @Schema(description = "Whether the email is verified", example = "true")
    private boolean emailVerified;

    @Schema(description = "Email address of the provider", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Username of the provider", example = "johndoe")
    private String username;

    @Schema(description = "Phone number of the provider", example = "+1234567890")
    private String phone;

    @Schema(description = "Secondary phone number of the provider", example = "+0987654321")
    private String phone2;

    @Schema(description = "Address of the provider", example = "123 Main St")
    private String address;

    @Schema(description = "Preferred language of the provider", example = "English")
    private String preferredLanguage;

    @Schema(description = "City of the provider", example = "New York")
    private String city;

    @Schema(description = "State of the provider", example = "NY")
    private String state;

    @Schema(description = "Country of the provider", example = "USA")
    private String country;

    @Schema(description = "Role of the provider", example = "PROVIDER")
    private EnumRoles roles;

    @Schema(description = "URL of the provider's image", example = "https://example.com/images/provider.jpg")
    private String imageUrl;

    // Provider-specific fields
    @Schema(description = "URL of the company logo", example = "https://example.com/logos/company.jpg")
    private String companyLogoUrl;

    @Schema(description = "Position of the primary contact", example = "Manager")
    private String primaryContactPosition;

    @Schema(description = "Department of the primary contact", example = "Operations")
    private String primaryContactDepartment;

    @Schema(description = "URL of the company banner", example = "https://example.com/banners/company.jpg")
    private String companyBannerUrl;

    @Schema(description = "Supplier code of the provider", example = "SUP123")
    private String supplierCode;

    @Schema(description = "Name of the company", example = "TechGlobal Inc.")
    private String companyName;

    @Schema(description = "Address of the company", example = "456 Tech St")
    private String companyAddress;

    @Schema(description = "Company registration number", example = "REG123456")
    private String companyRegistrationNumber;

    @Schema(description = "Description of the company", example = "A leading logistics provider")
    private String description;

    @Schema(description = "Zip code of the company", example = "10001")
    private String zipCode;

    @Schema(description = "Rating of the provider", example = "4.5")
    private Double rating;

    @Schema(description = "Number of ratings received", example = "10")
    private int numberOfRatings;

    @Schema(description = "List of service types offered by the provider", example = "[\"TRANSPORT\", \"SUPPLIES_DELIVERY\"]")
    private List<LogisticsServiceType> serviceTypes;

    @Schema(description = "List of service areas", example = "[\"New York\", \"Los Angeles\"]")
    private String serviceAreas;
}