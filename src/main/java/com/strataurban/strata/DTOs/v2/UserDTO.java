package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.EnumRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a User")
public class UserDTO {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Schema(description = "Title of the user", example = "Mr.")
    private String title;

    @Schema(description = "First name of the user", example = "John")
    private String firstName;

    @Schema(description = "Middle name of the user", example = "Michael")
    private String middleName;

    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;

    @Schema(description = "Whether the email is verified", example = "true")
    private boolean emailVerified;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Username of the user", example = "johndoe")
    private String username;

    @Schema(description = "Phone number of the user", example = "+1234567890")
    private String phone;

    @Schema(description = "Secondary phone number of the user", example = "+0987654321")
    private String phone2;

    @Schema(description = "Address of the user", example = "123 Main St")
    private String address;

    @Schema(description = "Preferred language of the user", example = "English")
    private String preferredLanguage;

    @Schema(description = "City of the user", example = "New York")
    private String city;

    @Schema(description = "State of the user", example = "NY")
    private String state;

    @Schema(description = "Country of the user", example = "USA")
    private String country;

    @Schema(description = "Role of the user", example = "CLIENT")
    private EnumRoles roles;

    @Schema(description = "URL of the user's image", example = "https://example.com/images/user.jpg")
    private String imageUrl;

    private String token;
}