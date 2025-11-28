package com.strataurban.strata.yoco_integration.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

// Response DTOs
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {

    private String id;

    private String redirectUrl;

    private String status;

    private Long amount;

    private String currency;

    private String paymentId;

    private String cancelUrl;

    private String successUrl;

    private String failureUrl;

    private Map<String, Object> metadata;

    private String merchantId;

    private Long totalDiscount;

    private Long totalTaxAmount;

    private Long subtotalAmount;

    private String externalId;

    private String processingMode;
}
