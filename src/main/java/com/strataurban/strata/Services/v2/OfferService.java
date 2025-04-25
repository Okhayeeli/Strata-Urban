package com.strataurban.strata.Services.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OfferService {
    Offer createOffer(Long bookingRequestId, Long providerId, Double price, String notes);
    Page<Offer> getOffersForBooking(Long bookingRequestId, Pageable pageable);
}