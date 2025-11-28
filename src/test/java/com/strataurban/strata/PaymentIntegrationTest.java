package com.strataurban.strata;

import com.strataurban.strata.yoco_integration.dtos.InitiatePaymentRequest;
import com.strataurban.strata.yoco_integration.dtos.PaymentResponse;
import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import com.strataurban.strata.yoco_integration.repositories.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for YOCO payment flow
 * Uses test mode with YOCO sandbox
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PaymentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Test
    public void testPaymentInitiation() {
        // Arrange
        InitiatePaymentRequest request = InitiatePaymentRequest.builder()
                .externalReference("TEST-ORDER-001")
                .customerId("CUST-123")
                .amount(new BigDecimal("100.00"))
                .currency("ZAR")
                .description("Test payment")
                .build();

        // Act
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                "/api/payments/initiate",
                request,
                PaymentResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCheckoutId());
        assertNotNull(response.getBody().getRedirectUrl());

        // Verify database entry
        PaymentTransaction transaction = transactionRepository
                .findByCheckoutId(response.getBody().getCheckoutId())
                .orElseThrow();

        assertEquals("TEST-ORDER-001", transaction.getExternalReference());
        assertEquals(PaymentTransaction.PaymentStatus.CREATED, transaction.getStatus());
    }

    @Test
    public void testIdempotency() {
        // Arrange
        InitiatePaymentRequest request = InitiatePaymentRequest.builder()
                .externalReference("TEST-ORDER-002")
                .customerId("CUST-456")
                .amount(new BigDecimal("50.00"))
                .currency("ZAR")
                .build();

        // Act - Make same request twice
        ResponseEntity<PaymentResponse> response1 = restTemplate.postForEntity(
                "/api/payments/initiate",
                request,
                PaymentResponse.class
        );

        ResponseEntity<PaymentResponse> response2 = restTemplate.postForEntity(
                "/api/payments/initiate",
                request,
                PaymentResponse.class
        );

        // Assert - Both should succeed but return same checkout
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());

        // Verify only one transaction was created
        long count = transactionRepository.count();
        assertEquals(1, count);
    }
}