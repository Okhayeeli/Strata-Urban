package com.strataurban.strata.DTOs;

import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoutesRequestDTO {

    private Long id;
    private String start;
    private String end;
    private BigDecimal price;
    private String supplierId;
    private String state;
    private String country;
    private String city;
}
