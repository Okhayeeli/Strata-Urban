package com.strataurban.strata.DTOs.v2;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class PasswordResetRequest {
    private String email;
}
