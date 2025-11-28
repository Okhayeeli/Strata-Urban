package com.strataurban.strata.yoco_integration.repositories;


import com.strataurban.strata.yoco_integration.entities.RefundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {

    Optional<RefundTransaction> findByRefundId(String refundId);

    List<RefundTransaction> findByPaymentTransactionId(Long paymentTransactionId);

    List<RefundTransaction> findByCheckoutId(String checkoutId);

    List<RefundTransaction> findByStatus(RefundTransaction.RefundStatus status);
}