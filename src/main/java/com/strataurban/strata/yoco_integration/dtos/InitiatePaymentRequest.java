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

    private String externalReference;
    private Long customerId;
    private BigDecimal amount;
    private Long offerId;
    private String currency;
    private String description;
    private MetaData metaData;
    private Long recipientId;
    private Long bookingId;
}