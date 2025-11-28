package com.strataurban.strata.yoco_integration.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

// Webhook DTOs
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {

    private String id;

    private String type;

    private String createdDate;

    private PaymentPayload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentPayload {
        private String id;
        private String type;
        private String createdDate;
        private Long amount;
        private String currency;
        private String status;
        private String mode;
        private Map<String, Object> metadata;
        private String paymentId;
    }
}
