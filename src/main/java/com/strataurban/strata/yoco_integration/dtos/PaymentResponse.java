package com.strataurban.strata.yoco_integration.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String checkoutId;

    private String redirectUrl;

    private String status;

    private String externalReference;

    private String message;
}
