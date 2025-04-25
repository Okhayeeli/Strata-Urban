package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Services.v2.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
    private final ProviderRepository providerRepository;

    @Autowired
    public OfferServiceImpl(OfferRepository offerRepository, BookingRepository bookingRepository, ProviderRepository providerRepository) {
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    public Offer createOffer(Long bookingRequestId, Long providerId, Double price, String notes) {
        BookingRequest booking = bookingRepository.findById(bookingRequestId)
                .orElseThrow(() -> new RuntimeException("Booking request not found with ID: " + bookingRequestId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Offers can only be submitted for PENDING bookings");
        }


        Offer offer = new Offer();
        offer.setBookingRequestId(bookingRequestId);
        offer.setProviderId(providerId);
        offer.setPrice(price);
        offer.setNotes(notes);

        return offerRepository.save(offer);
    }

    @Override
    public Page<Offer> getOffersForBooking(Long bookingRequestId, Pageable pageable) {
        return offerRepository.findByBookingRequestId(bookingRequestId, pageable);
    }
}