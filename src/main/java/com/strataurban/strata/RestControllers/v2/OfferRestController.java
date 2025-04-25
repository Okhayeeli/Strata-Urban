package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.AcceptOfferRequest;
import com.strataurban.strata.DTOs.v2.CreateOfferRequest;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
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

import java.util.List;


@RestController
@RequestMapping("/api/v2/offers")
@Tag(name = "Offer Management", description = "APIs for managing offers requests")
public class OfferRestController {
    private final BookingService bookingService;
    private final OfferService offerService;

    public OfferRestController(BookingService bookingService, OfferService offerService) {
        this.bookingService = bookingService;
        this.offerService = offerService;
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
        return ResponseEntity.ok(offerService.createOffer(bookingId, request.getProviderId(), request.getPrice(), request.getNotes()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get all offers for a booking", description = "Fetches all offers submitted for a specific booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offers retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Page<Offer>> getOffersForBooking(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(offerService.getOffersForBooking(id, pageable));
    }

    @PostMapping("/{id}/accept-offer")
    @Operation(summary = "Accept an offer for a booking", description = "Allows a client to accept an offer and mark the booking as CLAIMED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer accepted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot accept offer"),
            @ApiResponse(responseCode = "404", description = "Booking or offer not found")
    })
    public ResponseEntity<BookingRequest> acceptOffer(
            @PathVariable Long id,
            @RequestBody AcceptOfferRequest request) {
        return ResponseEntity.ok(bookingService.acceptOffer(id, request.getOfferId()));
    }
}
