package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to refresh access token")
public class RefreshTokenRequestDTO {
    @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String refreshToken;
}