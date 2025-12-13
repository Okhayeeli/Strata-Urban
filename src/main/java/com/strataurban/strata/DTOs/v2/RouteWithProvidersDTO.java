package com.strataurban.strata.DTOs.v2;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RouteWithProvidersDTO {
    private Long id;
    private String start;
    private String end;
    private BigDecimal price;
    private String state;
    private String country;
    private String city;
    private List<ProviderSummaryDTO> providers;
}