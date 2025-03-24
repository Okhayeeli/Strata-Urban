package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to change a user's password")
public class ChangePasswordRequest {

    @Schema(description = "Current password of the user", example = "oldPassword123")
    private String currentPassword;

    @Schema(description = "New password of the user", example = "newPassword123")
    private String newPassword;
}