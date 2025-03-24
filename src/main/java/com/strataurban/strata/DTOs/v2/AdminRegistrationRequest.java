package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.EnumRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to register an Admin or other internal roles")
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

    @Schema(description = "Password of the user", example = "password123")
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

    @Schema(description = "Role of the user", example = "ADMIN")
    private EnumRoles role;
}