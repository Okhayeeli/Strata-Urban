package com.strataurban.strata.Notifications;

import com.strataurban.strata.Entities.Passengers.Client;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Repositories.v2.ClientRepository;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageBuilder {

    private final ProviderRepository providerRepository;
    private final ClientRepository clientRepository;
    private final OfferRepository offerRepository;

    public String bookingRequest(String pickupLocation, String destination) {
        return String.format("New booking request from %s to %s", pickupLocation, destination);
    }

    public String bookingSuccessful(String pickupLocation, String destination) {
        return String.format("Your booking from %s to %s has been successfully initiated", pickupLocation, destination);
    }

    public String bookingConfirmed(String pickupLocation, String destination) {
        return String.format("Your booking from %s to %s has been confirmed", pickupLocation, destination);
    }

    public String bookingCancelled(String reason) {
        return String.format("Booking has been cancelled. Reason: %s", reason);
    }

    public String tripStarted(String driverName) {
        return String.format("Your trip with driver %s has started", driverName);
    }

    public String tripCompleted(String duration, String fare) {
        return String.format("Your trip has been completed. Duration: %s, Fare: %s", duration, fare);
    }

    public String paymentReceived(String amount) {
        return String.format("Payment of %s has been received", amount);
    }

    public String driverAssigned(String driverName, String vehicleInfo) {
        return String.format("Driver %s has been assigned to your booking. Vehicle: %s", driverName, vehicleInfo);
    }

    public String providerBookingRequest(Long providerId, String pickupLocation, String destination) {
        try {
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));

            return String.format("Dear %s, New booking request from %s to %s",
                    provider.getCompanyName(), pickupLocation, destination);
        } catch (Exception e) {
            log.error("Error building provider message for {}: {}", providerId, e.getMessage());
            return bookingRequest(pickupLocation, destination);
        }
    }

    public String offerReceived(BookingRequest booking, Long providerId, Long offerId) {
        try {
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));

            Client client = clientRepository.findById(booking.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found: " + booking.getClientId()));

            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Offer not found: " + offerId));

            return String.format(
                    "Dear %s, you have received an offer from %s for your booking from %s to %s.\n" +
                            "Offer details: Price - %s, Duration - %s, Valid until - %s.\n" +
                            "Please review and accept or decline the offer.",
                    client.getFirstName(),
                    provider.getCompanyName(),
                    booking.getPickUpLocation(),
                    booking.getDestination(),
                    offer.getFormattedPriceWithDiscount(),
                    offer.getEstimatedDuration(),
                    offer.getValidUntil()
            );
        } catch (Exception e) {
            log.error("Error building offer received message: {}", e.getMessage());
            return "You have received a new offer for your booking. Please check your bookings for details.";
        }
    }

    public String offerRejected(BookingRequest booking, Long providerId, String rejectionReason) {
        try {
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));

            String reasonSection = (rejectionReason != null && !rejectionReason.isBlank())
                    ? String.format("Reason provided by the client: %s.\n", rejectionReason)
                    : "";

            return String.format(
                    "Dear %s,\n\n" +
                            "Your offer for the booking from %s to %s was not accepted by the client.\n\n" +
                            "%s" +
                            "You may submit a new offer or review other available booking requests.\n\n" +
                            "Thank you for using our platform.",
                    provider.getCompanyName(),
                    booking.getPickUpLocation(),
                    booking.getDestination(),
                    reasonSection
            );
        } catch (Exception e) {
            log.error("Error building offer rejected message: {}", e.getMessage());
            return "Your offer was not accepted by the client. Please check your dashboard for more details.";
        }
    }

    public String offerAccepted(BookingRequest booking, Long providerId, Long offerId, String acceptanceNote) {
        try {
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));

            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Offer not found: " + offerId));

            String noteSection = (acceptanceNote != null && !acceptanceNote.isBlank())
                    ? String.format("Note from the client: %s.\n\n", acceptanceNote)
                    : "";

            return String.format(
                    "Dear %s,\n\n" +
                            "Good news! Your offer for the booking from %s to %s has been accepted by the client.\n\n" +
                            "%s" +
                            "Offer details:\n" +
                            "Price: %s\n" +
                            "Estimated Duration: %s\n\n" +
                            "Please proceed with the next steps to fulfill this booking.\n\n" +
                            "Thank you for using our platform.",
                    provider.getCompanyName(),
                    booking.getPickUpLocation(),
                    booking.getDestination(),
                    noteSection,
                    offer.getFormattedPriceWithDiscount(),
                    offer.getEstimatedDuration()
            );
        } catch (Exception e) {
            log.error("Error building offer accepted message: {}", e.getMessage());
            return "Your offer has been accepted by the client. Please check your dashboard for more details.";
        }
    }

    public String paymentSuccessfulClient(String amount, String bookingReference) {
        return String.format("Payment of %s has been processed successfully for booking #%s. " +
                "Your booking is now confirmed and being processed.", amount, bookingReference);
    }

    public String paymentSuccessfulProvider(String amount, String bookingReference) {
        return String.format("Payment of %s has been received for booking #%s. " +
                "You can now proceed with service fulfillment.", amount, bookingReference);
    }
}