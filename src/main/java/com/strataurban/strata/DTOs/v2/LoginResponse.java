package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "Response containing JWT tokens")
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    @Schema(description = "id of the user", example = "3")
    private Long id;

    @Schema(description = "Access token", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String accessToken;

    @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String refreshToken;

    @Schema(description = "User Role")
    private String role;

    private String firstName; // Add if missing
}
