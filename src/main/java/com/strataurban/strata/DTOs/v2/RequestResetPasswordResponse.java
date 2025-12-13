package com.strataurban.strata.DTOs.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestResetPasswordResponse {

    private Boolean success = false;
    private String subject;
    private String message;
    private String testToken;
}
