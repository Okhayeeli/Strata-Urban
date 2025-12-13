package com.strataurban.strata.DTOs.v2;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request for user login")
public class LoginRequest {
    @Schema(description = "Username or email of the user", example = "john.doe")
    private String usernameOrEmail;

    @Schema(description = "Password of the user", example = "password123")
    private String password;

    private String deviceToken;
}