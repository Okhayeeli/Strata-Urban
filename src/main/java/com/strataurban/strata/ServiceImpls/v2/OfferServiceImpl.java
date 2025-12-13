package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Enums.OfferStatus;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.Services.EmailService;
import com.strataurban.strata.Services.v2.OfferService;
import com.strataurban.strata.Services.v2.ProviderService;
import com.strataurban.strata.Services.v2.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.strataurban.strata.Enums.OfferStatus.*;

@Service
public class OfferServiceImpl implements OfferService {

    private static final Logger logger = LoggerFactory.getLogger(OfferServiceImpl.class);
    private static final int MAX_OFFERS_PER_PROVIDER = 3;

    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final ProviderService providerService;

    @Autowired
    public OfferServiceImpl(OfferRepository offerRepository, BookingRepository bookingRepository, EmailService emailService, UserService userService, ProviderService providerService) {
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.providerService = providerService;
    }

    @Override
    @Transactional
    public Offer createOffer(Long bookingRequestId, Long providerId, BigDecimal price, String notes,
                             LocalDateTime validUntil, Double discountPercentage, String websiteLink,
                             String estimatedDuration, String specialConditions, String currencyCode) {
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

        // Validate new fields
        if (validUntil != null && validUntil.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Offer expiration date must be in the future");
        }
        if (discountPercentage != null && (discountPercentage < 0 || discountPercentage > 100)) {
            throw new RuntimeException("Discount percentage must be between 0 and 100");
        }
        if (websiteLink != null && !websiteLink.matches("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$")) {
            throw new RuntimeException("Invalid website URL format");
        }

        Offer offer = new Offer();
        offer.setBookingRequestId(bookingRequestId);
        offer.setProviderId(providerId);
        offer.setPrice(price);
        offer.setNotes(notes);
        offer.setStatus(PENDING);
        offer.setValidUntil(validUntil);
        offer.setDiscountPercentage(discountPercentage);
        offer.setWebsiteLink(websiteLink);
        offer.setEstimatedDuration(estimatedDuration);
        offer.setSpecialConditions(specialConditions);
        offer.setCurrencyCode(currencyCode);
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


        emailService.sendOfferEmail(userService.getUserById(booking.getClientId()).getFirstName(),offer, providerService.getProviderById(providerId).getCompanyName());

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
        offers.sort((o1, o2) -> {
            int index1 = offerIdList.indexOf(o1.getId());
            int index2 = offerIdList.indexOf(o2.getId());
            return Integer.compare(index1, index2);
        });

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
    public Offer updateOffer(Long offerId, Long providerId, BigDecimal price, String notes, LocalDateTime validUntil,
                             Double discountPercentage, String websiteLink, String estimatedDuration,
                             String specialConditions) {
        logger.info("Updating offer with ID: {} by provider ID: {}", offerId, providerId);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));

        // Validate provider ownership
        if (!offer.getProviderId().equals(providerId)) {
            throw new SecurityException("Provider ID: " + providerId + " is not authorized to update offer ID: " + offerId);
        }

        // Validate new fields
        if (validUntil != null && validUntil.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Offer expiration date must be in the future");
        }
        if (discountPercentage != null && (discountPercentage < 0 || discountPercentage > 100)) {
            throw new RuntimeException("Discount percentage must be between 0 and 100");
        }
        if (websiteLink != null && !websiteLink.matches("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$")) {
            throw new RuntimeException("Invalid website URL format");
        }

        offer.setPrice(price);
        offer.setNotes(notes);
        offer.setValidUntil(validUntil);
        offer.setDiscountPercentage(discountPercentage);
        offer.setWebsiteLink(websiteLink);
        offer.setEstimatedDuration(estimatedDuration);
        offer.setSpecialConditions(specialConditions);

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

    @Override
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

    @Override
    @Transactional
    public void disableOtherOffers(Long bookingRequestId, Long acceptedOfferId) {
        logger.info("Updating other offers for bookingRequestId: {}, keeping offerId: {}", bookingRequestId, acceptedOfferId);
        BookingRequest booking = bookingRepository.findById(bookingRequestId)
                .orElseThrow(() -> new RuntimeException("Booking request not found with ID: " + bookingRequestId));

        String offerIds = booking.getOfferIds();
        if (offerIds == null || offerIds.isEmpty()) {
            logger.info("No other offers to update for bookingRequestId: {}", bookingRequestId);
            return;
        }

        List<Long> offerIdList = Arrays.stream(offerIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .filter(id -> !id.equals(acceptedOfferId))
                .collect(Collectors.toList());

        Offer offerToAccept = offerRepository.findById(acceptedOfferId)
                .orElseThrow(() -> new RuntimeException("No offer with id " + acceptedOfferId + " found"));
        offerToAccept.setStatus(ACCEPTED);
        offerRepository.save(offerToAccept);

        if (!offerIdList.isEmpty()) {
            List<Offer> offersToReject = offerRepository.findAllById(offerIdList);
            for (Offer offer : offersToReject) {
                offer.setStatus(REJECTED);
            }
            offerRepository.saveAll(offersToReject);

            booking.setOfferIds(String.valueOf(acceptedOfferId));
            bookingRepository.save(booking);
            logger.info("Updated {} other offers to REJECTED status, updated offerIds to: {}", offerIdList.size(), booking.getOfferIds());
        }
    }

    @Override
    public Page<Offer> getOfferByProviderId(
            SecurityUserDetails userDetails,
            OfferStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long providerId,
            Pageable pageable
    ) {

        if(userDetails.getRole()== EnumRoles.PROVIDER){
            logger.info("Fetching offers for provider ID: {} with filters", userDetails.getId());
            return offerRepository.findOffersByFilters(userDetails.getId(), status, fromDate, toDate, pageable);
        }
            logger.info("Fetching offers for provider ID: {} with filters", userDetails.getId());
            if (providerId==null){
                throw new IllegalArgumentException("Provider ID is null, Provider Id must be specified for Admin Calls");
            }
            return offerRepository.findOffersByFilters(providerId, status, fromDate, toDate, pageable);
    }


    @Override
    public boolean isAuthorizedProviderOffer(Long offerId, Long providerId) {
        logger.info("Checking if provider ID: {} is authorized for offer ID: {}", providerId, offerId);
        return offerRepository.findById(offerId)
                .map(offer -> offer.getProviderId().equals(providerId))
                .orElse(false);
    }

    @Override
    public boolean isAuthorizedClientOffer(Long offerId, Long clientId) {
        logger.info("Checking if client ID: {} is authorized for offer ID: {}", clientId, offerId);
        return offerRepository.findById(offerId)
                .map(offer -> bookingRepository.findById(offer.getBookingRequestId())
                        .map(booking -> booking.getClientId().equals(clientId))
                        .orElse(false))
                .orElse(false);
    }
}