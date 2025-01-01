package com.strataurban.strata.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupRequest {

    String username;
    String password;
    String confirmPassword;
    String email;
}
