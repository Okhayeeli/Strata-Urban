package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Enums.OfferStatus;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Page<Offer> findByBookingRequestId(Long bookingRequestId, Pageable pageable);

    List<Offer> findAllById(@NonNull Iterable<Long> ids);
    Long countByBookingRequestIdAndProviderId(Long bookingRequestId, Long providerId);

    Page<Offer> findByProviderId(Long providerId, Pageable pageable);

    Offer findByTransactionReference(String transactionReference);


    Boolean existsByTransactionReference(String transactionReference);

    @Query("""
       SELECT o FROM Offer o
       WHERE o.providerId = :providerId
       AND (:status IS NULL OR o.status = :status)
       AND (:fromDate IS NULL OR o.createdDate >= :fromDate)
       AND (:toDate IS NULL OR o.createdDate <= :toDate)
       """)
    Page<Offer> findOffersByFilters(
            @Param("providerId") Long providerId,
            @Param("status") OfferStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

}

