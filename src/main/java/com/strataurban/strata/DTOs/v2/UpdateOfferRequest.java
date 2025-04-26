package com.strataurban.strata.DTOs.v2;

import lombok.Data;

@Data
public class UpdateOfferRequest {
    private Double price;
    private String notes;
}