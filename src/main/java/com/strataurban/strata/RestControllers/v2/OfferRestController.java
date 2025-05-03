package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.AcceptOfferRequest;
import com.strataurban.strata.DTOs.v2.BookingWithOffersResponse;
import com.strataurban.strata.DTOs.v2.CreateOfferRequest;
import com.strataurban.strata.DTOs.v2.UpdateOfferRequest;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Services.v2.BookingService;
import com.strataurban.strata.Services.v2.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/offers")
@Tag(name = "Offer Management", description = "APIs for managing offer requests")
public class OfferRestController {

    private static final Logger logger = LoggerFactory.getLogger(OfferRestController.class);

    private final BookingService bookingService;
    private final OfferService offerService;

    private final OfferRepository offerRepository;

    @Autowired
    public OfferRestController(BookingService bookingService, OfferService offerService, OfferRepository offerRepository) {
        this.bookingService = bookingService;
        this.offerService = offerService;
        this.offerRepository = offerRepository;
    }

    @PostMapping("/{bookingId}")
    @Operation(summary = "Create an offer for a booking", description = "Allows a provider to submit an offer for a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid offer request"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Offer> createOffer(
            @PathVariable Long bookingId,
            @RequestBody CreateOfferRequest request) {
        logger.info("Creating offer for booking ID: {}", bookingId);
        Offer offer = offerService.createOffer(bookingId, request.getProviderId(), request.getPrice(), request.getNotes());
        logger.info("Created offer: {}", offer);
        return ResponseEntity.ok(offer);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get all offers for a booking", description = "Fetches all offers submitted for a specific booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offers retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Page<Offer>> getOffersForBooking(
            @PathVariable Long bookingId,
            Pageable pageable) {
        logger.info("Fetching offers for booking ID: {}", bookingId);
        Page<Offer> offers = offerService.getOffersForBooking(bookingId, pageable);
        logger.info("Retrieved {} offers for booking ID: {}", offers.getTotalElements(), bookingId);
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/single/{offerId}")
    @Operation(summary = "Get a single offer by ID", description = "Fetches details of a specific offer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Offer not found")
    })
    public ResponseEntity<Offer> getOfferById(@PathVariable Long offerId) {
        logger.info("Fetching offer with ID: {}", offerId);
        Offer offer = offerService.getOfferById(offerId);
        logger.info("Retrieved offer: {}", offer);
        return ResponseEntity.ok(offer);
    }

    @PutMapping("/{offerId}")
    @Operation(summary = "Update an offer", description = "Allows a provider to update an existing offer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid offer request"),
            @ApiResponse(responseCode = "404", description = "Offer not found")
    })
    public ResponseEntity<Offer> updateOffer(
            @PathVariable Long offerId,
            @RequestBody UpdateOfferRequest request) {
        logger.info("Updating offer with ID: {}", offerId);
        Offer updatedOffer = offerService.updateOffer(offerId, request.getPrice(), request.getNotes());
        logger.info("Updated offer: {}", updatedOffer);
        return ResponseEntity.ok(updatedOffer);
    }

    @DeleteMapping("/{offerId}/booking/{bookingId}")
    @Operation(summary = "Delete an offer", description = "Allows a provider to delete an offer and remove it from the booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Offer or booking not found")
    })
    public ResponseEntity<Void> deleteOffer(
            @PathVariable Long offerId,
            @PathVariable Long bookingId) {
        logger.info("Deleting offer with ID: {} for booking ID: {}", offerId, bookingId);
        offerService.deleteOffer(offerId, bookingId);
        logger.info("Deleted offer with ID: {}", offerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bookingId}/accept-offer")
    @Operation(summary = "Accept an offer for a booking", description = "Allows a client to accept an offer and mark the booking as CLAIMED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer accepted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot accept offer"),
            @ApiResponse(responseCode = "404", description = "Booking or offer not found")
    })
    public ResponseEntity<BookingRequest> acceptOffer(
            @PathVariable Long bookingId,
            @RequestBody AcceptOfferRequest request) {
        logger.info("Accepting offer ID: {} for booking ID: {}", request.getOfferId(), bookingId);
        BookingRequest booking = bookingService.acceptOffer(bookingId, request.getOfferId());
        logger.info("Accepted offer for booking: {}", booking);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get booking with offers", description = "Fetches a booking with its associated offers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking and offers retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingWithOffersResponse> getBookingWithOffers(@PathVariable Long bookingId) {
        logger.info("Fetching booking with offers for booking ID: {}", bookingId);
        BookingRequest booking = bookingService.getBookingById(bookingId);
        List<Offer> offers = List.of();
        String offerIds = booking.getOfferIds();
        if (offerIds != null && !offerIds.isEmpty()) {
            List<Long> offerIdList = Arrays.stream(offerIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            offers = offerRepository.findAllById(offerIdList);
            // Maintain order based on offerIds
            offers.sort((o1, o2) -> {
                int index1 = offerIdList.indexOf(o1.getId());
                int index2 = offerIdList.indexOf(o2.getId());
                return Integer.compare(index1, index2);
            });
        }
        BookingWithOffersResponse response = new BookingWithOffersResponse(booking, offers);
        logger.info("Retrieved booking with {} offers for booking ID: {}", offers.size(), bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/provider")
    @Operation(summary = "Get a single offer by Provider ID", description = "Fetches details of a specific offer using Provider Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer(s) retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Offer(s) not found")
    })
    public ResponseEntity<Page<Offer>> getOffersByProviderId(@RequestParam Long providerId, Pageable pageable) {
        logger.info("Fetching offer with provider ID: {}", providerId);
        Page<Offer> offers = offerService.getOfferByProviderId(providerId, pageable);
        logger.info("Retrieved offer for Providers: {}", offers);
        return ResponseEntity.ok(offers);
    }
}