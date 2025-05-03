package com.strataurban.strata.Services.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfferService {
    Offer createOffer(Long bookingRequestId, Long providerId, Double price, String notes);

    Page<Offer> getOffersForBooking(Long bookingRequestId, Pageable pageable);

    Offer getOfferById(Long offerId);

    Offer updateOffer(Long offerId, Double price, String notes);

    void deleteOffer(Long offerId, Long bookingRequestId);

    void deleteOtherOffers(Long bookingRequestId, Long acceptedOfferId);

    Page<Offer> getOfferByProviderId(Long providerId, Pageable pageable);
}
