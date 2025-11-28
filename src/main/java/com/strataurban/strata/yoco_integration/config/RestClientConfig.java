package com.strataurban.strata.yoco_integration.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;


@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final YocoProperties yocoProperties;

    @Bean
    public RestTemplate yocoRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(yocoProperties.getApi().getBaseUrl())
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .additionalInterceptors(authInterceptor())
                .build();
    }

    private ClientHttpRequestInterceptor authInterceptor() {
        return (request, body, execution) -> {
            // Set Authorization header
            request.getHeaders().setBearerAuth(yocoProperties.getApi().getSecretKey());

            // Only set Content-Type if not already set (avoid duplicates)
            if (request.getHeaders().getContentType() == null) {
                request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            }

            return execution.execute(request, body);
        };
    }
}