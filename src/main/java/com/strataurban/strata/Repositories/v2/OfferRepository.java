package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Page<Offer> findByBookingRequestId(Long bookingRequestId, Pageable pageable);
}

