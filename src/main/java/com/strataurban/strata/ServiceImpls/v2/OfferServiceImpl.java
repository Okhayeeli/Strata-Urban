package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;

import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Services.v2.OfferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfferServiceImpl implements OfferService {

    private static final Logger logger = LoggerFactory.getLogger(OfferServiceImpl.class);
    private static final int MAX_OFFERS_PER_PROVIDER = 3;

    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public OfferServiceImpl(OfferRepository offerRepository, BookingRepository bookingRepository) {
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public Offer createOffer(Long bookingRequestId, Long providerId, Double price, String notes) {
        logger.info("Creating offer for bookingRequestId: {}, providerId: {}", bookingRequestId, providerId);
        BookingRequest booking = bookingRepository.findById(bookingRequestId)
                .orElseThrow(() -> new RuntimeException("Booking request not found with ID: " + bookingRequestId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Offers can only be submitted for PENDING bookings");
        }

        // Check if provider has reached the offer limit
        long existingOffers = offerRepository.countByBookingRequestIdAndProviderId(bookingRequestId, providerId);
        if (existingOffers >= MAX_OFFERS_PER_PROVIDER) {
            throw new RuntimeException("Provider ID: " + providerId + " has reached the maximum of " + MAX_OFFERS_PER_PROVIDER + " offers for booking ID: " + bookingRequestId);
        }

        Offer offer = new Offer();
        offer.setBookingRequestId(bookingRequestId);
        offer.setProviderId(providerId);
        offer.setPrice(price);
        offer.setNotes(notes);

        Offer savedOffer = offerRepository.save(offer);
        logger.info("Created offer: {}", savedOffer);

        // Append offer ID to BookingRequest.offerIds
        String offerId = String.valueOf(savedOffer.getId());
        String currentOfferIds = booking.getOfferIds();
        if (currentOfferIds == null || currentOfferIds.isEmpty()) {
            booking.setOfferIds(offerId);
        } else {
            booking.setOfferIds(currentOfferIds + "," + offerId);
        }
        bookingRepository.save(booking);
        logger.info("Updated BookingRequest.offerIds: {}", booking.getOfferIds());

        return savedOffer;
    }

    @Override
    public Page<Offer> getOffersForBooking(Long bookingRequestId, Pageable pageable) {
        logger.info("Fetching offers for bookingRequestId: {}", bookingRequestId);
        BookingRequest booking = bookingRepository.findById(bookingRequestId)
                .orElseThrow(() -> new RuntimeException("Booking request not found with ID: " + bookingRequestId));

        String offerIds = booking.getOfferIds();
        if (offerIds == null || offerIds.isEmpty()) {
            logger.info("No offers found for bookingRequestId: {}", bookingRequestId);
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Long> offerIdList = Arrays.stream(offerIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<Offer> offers = offerRepository.findAllById(offerIdList);
        // Maintain order based on offerIds
        offers.sort((o1, o2) -> {
            int index1 = offerIdList.indexOf(o1.getId());
            int index2 = offerIdList.indexOf(o2.getId());
            return Integer.compare(index1, index2);
        });

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), offers.size());
        List<Offer> pagedOffers = start < offers.size() ? offers.subList(start, end) : List.of();

        logger.info("Retrieved {} offers for bookingRequestId: {}", pagedOffers.size(), bookingRequestId);
        return new PageImpl<>(pagedOffers, pageable, offers.size());
    }

    @Override
    public Offer getOfferById(Long offerId) {
        logger.info("Fetching offer with ID: {}", offerId);
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));
    }

    @Override
    @Transactional
    public Offer updateOffer(Long offerId, Double price, String notes) {
        logger.info("Updating offer with ID: {}", offerId);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));

        offer.setPrice(price);
        offer.setNotes(notes);
        Offer updatedOffer = offerRepository.save(offer);
        logger.info("Updated offer: {}", updatedOffer);
        return updatedOffer;
    }

    @Override
    @Transactional
    public void deleteOffer(Long offerId, Long bookingRequestId) {
        logger.info("Deleting offer with ID: {} for bookingRequestId: {}", offerId, bookingRequestId);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));

        BookingRequest booking = bookingRepository.findById(bookingRequestId)
                .orElseThrow(() -> new RuntimeException("Booking request not found with ID: " + bookingRequestId));

        // Remove offerId from BookingRequest.offerIds
        String offerIds = booking.getOfferIds();
        if (offerIds != null && !offerIds.isEmpty()) {
            List<String> offerIdList = Arrays.stream(offerIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.equals(String.valueOf(offerId)))
                    .collect(Collectors.toList());
            booking.setOfferIds(offerIdList.isEmpty() ? null : String.join(",", offerIdList));
            bookingRepository.save(booking);
            logger.info("Updated BookingRequest.offerIds: {}", booking.getOfferIds());
        }

        offerRepository.delete(offer);
        logger.info("Deleted offer with ID: {}", offerId);
    }

    @Transactional
    public void deleteOtherOffers(Long bookingRequestId, Long acceptedOfferId) {
        logger.info("Deleting other offers for bookingRequestId: {}, keeping offerId: {}", bookingRequestId, acceptedOfferId);
        BookingRequest booking = bookingRepository.findById(bookingRequestId)
                .orElseThrow(() -> new RuntimeException("Booking request not found with ID: " + bookingRequestId));

        String offerIds = booking.getOfferIds();
        if (offerIds == null || offerIds.isEmpty()) {
            logger.info("No other offers to delete for bookingRequestId: {}", bookingRequestId);
            return;
        }

        List<Long> offerIdList = Arrays.stream(offerIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .filter(id -> !id.equals(acceptedOfferId))
                .collect(Collectors.toList());

        if (!offerIdList.isEmpty()) {
            offerRepository.deleteAllById(offerIdList);
            booking.setOfferIds(String.valueOf(acceptedOfferId));
            bookingRepository.save(booking);
            logger.info("Deleted {} other offers, updated offerIds to: {}", offerIdList.size(), booking.getOfferIds());
        }
    }
}