package com.strataurban.strata.DTOs.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to register a Client")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientRegistrationRequest {

    @Schema(description = "Title of the client", example = "Mr.")
    private String title;

    @Schema(description = "First name of the client", example = "John")
    private String firstName;

    @Schema(description = "Middle name of the client", example = "Michael")
    private String middleName;

    @Schema(description = "Last name of the client", example = "Doe")
    private String lastName;

    @Schema(description = "Email address of the client", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Username of the client", example = "johndoe")
    private String username;

    @Schema(description = "Password of the client", example = "password123")
    private String password;

    @Schema(description = "Primary phone number of the client", example = "+1234567890")
    private String phone;

    @Schema(description = "Secondary phone number of the client", example = "+0987654321")
    private String phone2;

    @Schema(description = "Address of the client", example = "456 Client Rd")
    private String address;

    @Schema(description = "Preferred language of the client", example = "English")
    private String preferredLanguage;

    @Schema(description = "City of the client", example = "Los Angeles")
    private String city;

    @Schema(description = "State of the client", example = "CA")
    private String state;

    @Schema(description = "Country of the client", example = "USA")
    private String country;

    @Schema(description = "URL of the client's image", example = "https://example.com/images/client.jpg")
    private String imageUrl;

    private String topSecretPasswordForTest;
}