package com.strataurban.strata.yoco_integration.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {

    private String createdDate;

    private String id;

    private PaymentPayload payload;

    private String type;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentPayload {
        private Long amount;

        private String createdDate;

        private String currency;

        private String id;

        private Map<String, Object> metadata;

        private String mode;

        private PaymentMethodDetails paymentMethodDetails;

        private String status;

        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodDetails {
        private Card card;

        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Card {
        private Integer expiryMonth;

        private Integer expiryYear;

        private String maskedCard;

        private String scheme;
    }
}