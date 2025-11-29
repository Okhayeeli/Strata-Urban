package com.strataurban.strata.Services.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OfferService {

    /**
     * Creates a new offer for a booking request.
     *
     * @param bookingRequestId The ID of the booking request.
     * @param providerId The ID of the provider creating the offer.
     * @param price The price of the offer.
     * @param notes Additional notes for the offer.
     * @param validUntil The expiration date and time of the offer.
     * @param discountPercentage The discount percentage, if any.
     * @param websiteLink A link to the provider's website, if provided.
     * @param estimatedDuration The estimated duration of the service.
     * @param specialConditions Any special conditions for the offer.
     * @return The created Offer entity.
     */
    Offer createOffer(Long bookingRequestId, Long providerId, BigDecimal price, String notes,
                      LocalDateTime validUntil, Double discountPercentage, String websiteLink,
                      String estimatedDuration, String specialConditions);

    /**
     * Retrieves all offers for a specific booking request, paginated.
     *
     * @param bookingRequestId The ID of the booking request.
     * @param pageable Pagination and sorting parameters.
     * @return A Page of Offer entities.
     */
    Page<Offer> getOffersForBooking(Long bookingRequestId, Pageable pageable);

    /**
     * Retrieves a single offer by its ID.
     *
     * @param offerId The ID of the offer.
     * @return The Offer entity.
     */
    Offer getOfferById(Long offerId);

    /**
     * Updates an existing offer.
     *
     * @param offerId The ID of the offer to update.
     * @param providerId The ID of the provider updating the offer.
     * @param price The updated price.
     * @param notes The updated notes.
     * @param validUntil The updated expiration date and time.
     * @param discountPercentage The updated discount percentage.
     * @param websiteLink The updated website link.
     * @param estimatedDuration The updated estimated duration.
     * @param specialConditions The updated special conditions.
     * @return The updated Offer entity.
     */
    Offer updateOffer(Long offerId, Long providerId, BigDecimal price, String notes, LocalDateTime validUntil,
                      Double discountPercentage, String websiteLink, String estimatedDuration,
                      String specialConditions);

    /**
     * Deletes an offer and removes it from the associated booking.
     *
     * @param offerId The ID of the offer to delete.
     * @param bookingRequestId The ID of the associated booking request.
     */
    void deleteOffer(Long offerId, Long bookingRequestId);

    /**
     * Deletes all offers for a booking except the accepted offer.
     *
     * @param bookingRequestId The ID of the booking request.
     * @param acceptedOfferId The ID of the accepted offer to keep.
     */
    void deleteOtherOffers(Long bookingRequestId, Long acceptedOfferId);

    /**
     * Disables (marks as REJECTED) all offers for a booking except the accepted offer, which is marked as ACCEPTED.
     *
     * @param bookingRequestId The ID of the booking request.
     * @param acceptedOfferId The ID of the accepted offer.
     */
    void disableOtherOffers(Long bookingRequestId, Long acceptedOfferId);

    /**
     * Retrieves all offers submitted by a specific provider, paginated.
     *
     * @param providerId The ID of the provider.
     * @param pageable Pagination and sorting parameters.
     * @return A Page of Offer entities.
     */
    Page<Offer> getOfferByProviderId(Long providerId, Pageable pageable);

    /**
     * Checks if a provider is authorized to access or modify an offer.
     *
     * @param offerId The ID of the offer.
     * @param providerId The ID of the provider.
     * @return True if the provider is authorized, false otherwise.
     */
    boolean isAuthorizedProviderOffer(Long offerId, Long providerId);

    /**
     * Checks if a client is authorized to access an offer (i.e., owns the associated booking).
     *
     * @param offerId The ID of the offer.
     * @param clientId The ID of the client.
     * @return True if the client is authorized, false otherwise.
     */
    boolean isAuthorizedClientOffer(Long offerId, Long clientId);
}