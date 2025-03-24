package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.strataurban.strata.Enums.LogisticsServiceType;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to register a Provider")
public class ProviderRegistrationRequest {

    @Schema(description = "Title of the provider", example = "Ms.")
    private String title;

    @Schema(description = "First name of the provider", example = "Jane")
    private String firstName;

    @Schema(description = "Middle name of the provider", example = "Ann")
    private String middleName;

    @Schema(description = "Last name of the provider", example = "Smith")
    private String lastName;

    @Schema(description = "Email address of the provider", example = "jane.smith@example.com")
    private String email;

    @Schema(description = "Username of the provider", example = "janesmith")
    private String username;

    @Schema(description = "Password of the provider", example = "password123")
    private String password;

    @Schema(description = "Primary phone number of the provider", example = "+1234567890")
    private String phone;

    @Schema(description = "Secondary phone number of the provider", example = "+0987654321")
    private String phone2;

    @Schema(description = "Address of the provider", example = "123 Business St")
    private String address;

    @Schema(description = "City of the provider", example = "New York")
    private String city;

    @Schema(description = "State of the provider", example = "NY")
    private String state;

    @Schema(description = "Zip code of the provider", example = "10001")
    private String zipCode;

    @Schema(description = "Country of the provider", example = "USA")
    private String country;

    @Schema(description = "Preferred language of the provider", example = "English")
    private String preferredLanguage;

    @Schema(description = "URL of the provider's image", example = "https://example.com/images/provider.jpg")
    private String imageUrl;

    @Schema(description = "URL of the company logo", example = "https://example.com/images/logo.jpg")
    private String companyLogoUrl;

    @Schema(description = "Position of the primary contact", example = "CEO")
    private String primaryContactPosition;

    @Schema(description = "Department of the primary contact", example = "Executive")
    private String primaryContactDepartment;

    @Schema(description = "URL of the company banner", example = "https://example.com/images/banner.jpg")
    private String companyBannerUrl;

    @Schema(description = "Supplier code of the company", example = "SUP123")
    private String supplierCode;

    @Schema(description = "Name of the company", example = "TechGlobal Inc.")
    private String companyName;

    @Schema(description = "Address of the company", example = "456 Business Rd")
    private String companyAddress;

    @Schema(description = "Type of business of the company", example = "Logistics")
    private String companyBusinessType;

    @Schema(description = "Company business phone number", example = "+1234567890")
    private String companyBusinessPhone;

    @Schema(description = "Company business website", example = "https://example.com")
    private String companyBusinessWebsite;

    @Schema(description = "Company business email address", example = "company@gmail.com")
    private String companyBusinessEmail;

    @Schema(description = "Company registration number", example = "REG123456")
    private String companyRegistrationNumber;

    @Schema(description = "Description of the company", example = "A leading logistics provider")
    private String description;

    @Schema(description = "List of logistics service types offered by the provider", example = "[\"FREIGHT_FORWARDING\", \"WAREHOUSING\"]")
    private List<LogisticsServiceType> serviceTypes;

    @Schema(description = "List of service area IDs where the provider operates", example = "[1, 2]")
    private List<Long> serviceAreaIds;
}