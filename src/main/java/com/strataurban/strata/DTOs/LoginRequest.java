package com.strataurban.strata.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {

    String username;
    String password;
}
