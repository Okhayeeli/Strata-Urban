package com.strataurban.strata.DTOs.v2;

import lombok.Data;

@Data
public class RequestResetPasswordResponse {

    private Boolean success = false;
    private String subject;
    private String token;
    private String message;


}
