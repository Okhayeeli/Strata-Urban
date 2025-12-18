package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Passengers.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    Optional<Receipt> findByPaymentTransactionId(Long paymentTransactionId);

    Optional<Receipt> findByBookingId(Long bookingId);

    List<Receipt> findByClientId(Long clientId);

    List<Receipt> findByProviderId(Long providerId);

    List<Receipt> findByClientIdAndGeneratedAtBetween(
            Long clientId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Receipt> findByProviderIdAndGeneratedAtBetween(
            Long providerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Receipt> findByStatus(String status);

    boolean existsByPaymentTransactionId(Long paymentTransactionId);

    boolean existsByBookingId(Long bookingId);
}