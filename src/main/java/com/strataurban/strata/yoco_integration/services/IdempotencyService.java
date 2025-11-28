package com.strataurban.strata.yoco_integration.services;

import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import com.strataurban.strata.yoco_integration.repositories.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service to handle idempotency for payment operations
 * Prevents duplicate charges from double-clicks or network retries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final PaymentTransactionRepository transactionRepository;

    /**
     * Checks if a transaction with this idempotency key already exists
     */
    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * Validates if operation can proceed based on idempotency key
     */
    public boolean canProceed(String idempotencyKey) {
        return findByIdempotencyKey(idempotencyKey).isEmpty();
    }
}