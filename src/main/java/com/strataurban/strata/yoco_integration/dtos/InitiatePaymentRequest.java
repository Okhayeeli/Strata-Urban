package com.strataurban.strata.yoco_integration.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {

    private String externalReference; // Your order/invoice ID

    private String customerId;

    private BigDecimal amount;

    private String currency;

    private String description;

    private String cancelUrl;

    private String successUrl;

    private String failureUrl;
}