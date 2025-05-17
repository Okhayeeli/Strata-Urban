package com.strataurban.strata.DTOs.v2;

import lombok.Data;

@Data
public class DriverResponse {
    private Long id;
    private String fullName;
    private String address;
    private String email;
}