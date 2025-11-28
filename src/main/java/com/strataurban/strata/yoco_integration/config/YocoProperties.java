package com.strataurban.strata.yoco_integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "yoco")
public class YocoProperties {
    private Api api = new Api();
    private Webhook webhook = new Webhook();
    private Transaction transaction = new Transaction();

    @Data
    public static class Api {
        private String baseUrl;
        private String secretKey;
        private String webhookSecret;
    }

    @Data
    public static class Webhook {
        private boolean signatureValidationEnabled = true;
        private int timestampToleranceSeconds = 180;
    }

    @Data
    public static class Transaction {
        private boolean idempotencyEnabled = true;
        private int idempotencyTtlHours = 24;
    }
}
