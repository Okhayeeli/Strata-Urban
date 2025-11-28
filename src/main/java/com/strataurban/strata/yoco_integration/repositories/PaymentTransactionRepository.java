package com.strataurban.strata.yoco_integration.repositories;

import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByCheckoutId(String checkoutId);

    Optional<PaymentTransaction> findByExternalReference(String externalReference);

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    List<PaymentTransaction> findByCustomerId(String customerId);

    List<PaymentTransaction> findByStatus(PaymentTransaction.PaymentStatus status);

    @Query("SELECT p FROM PaymentTransaction p WHERE p.status = :status " +
            "AND p.createdAt < :beforeDate")
    List<PaymentTransaction> findStaleTransactions(
            PaymentTransaction.PaymentStatus status,
            LocalDateTime beforeDate);

    @Query("SELECT COUNT(p) FROM PaymentTransaction p WHERE p.status = :status " +
            "AND p.createdAt > :afterDate")
    long countByStatusAndCreatedAfter(
            PaymentTransaction.PaymentStatus status,
            LocalDateTime afterDate);
}