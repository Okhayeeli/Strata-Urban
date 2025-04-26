package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Page<Offer> findByBookingRequestId(Long bookingRequestId, Pageable pageable);

    List<Offer> findAllById(Iterable<Long> ids);
    Long countByBookingRequestIdAndProviderId(Long bookingRequestId, Long providerId);
}

