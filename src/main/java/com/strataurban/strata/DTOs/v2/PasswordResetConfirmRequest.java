package com.strataurban.strata.DTOs.v2;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirmRequest {
    private String token;
    private String newPassword;
}