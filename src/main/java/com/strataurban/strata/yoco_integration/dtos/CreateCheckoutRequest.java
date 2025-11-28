package com.strataurban.strata.yoco_integration.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateCheckoutRequest {

    private Long amount; // Amount in cents

    private String currency;

    private String cancelUrl;

    private String successUrl;

    private String failureUrl;

    private Map<String, Object> metadata;

    private Long totalDiscount; // Display only

    private Long totalTaxAmount; // Display only

    private Long subtotalAmount; // Display only
}



