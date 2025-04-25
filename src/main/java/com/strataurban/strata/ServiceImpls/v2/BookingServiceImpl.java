package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.DTOs.v2.ContactRequest;
import com.strataurban.strata.DTOs.v2.DriverAssignmentRequest;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Enums.Status;
import com.strataurban.strata.Enums.TripStatus;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Services.v2.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private final BookingRepository bookingRepository;

    @Autowired
    private final OfferRepository offerRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, OfferRepository offerRepository) {
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    public BookingRequest createBooking(BookingRequest bookingRequest, Long clientId) {
        // Set the client ID (assuming BookingRequest has a clientId field)
        // Note: You might need to add a clientId field to the BookingRequest entity
        // For now, we'll assume the clientId is part of additionalNotes or metadata
        bookingRequest.setClientId(clientId);

        //TODO After Authentication
        bookingRequest.setStatus(BookingStatus.PENDING); // Default status
        return bookingRepository.save(bookingRequest);
    }

    @Override
    public List<BookingRequest> getClientBookings(Long clientId) {
        return bookingRepository.findByClientId(clientId);
    }

    @Override
    public List<BookingRequest> getProviderBookings(Long providerId) {
        return bookingRepository.findByProviderId(providerId);
    }

    @Override
    public BookingRequest getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
    }

    @Override
    public BookingRequest updateBookingStatus(Long id, BookingStatus status) {
        BookingRequest booking = getBookingById(id);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    @Override
    public BookingRequest confirmBooking(Long id) {
        BookingRequest booking = getBookingById(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Override
    public BookingRequest claimBooking(Long id, Long providerId) {
        BookingRequest booking = getBookingById(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CLAIMED);
        //TODO After Authentication, use the Provider Id
        booking.setProviderId(providerId);
        return bookingRepository.save(booking);
    }


    @Override
    public BookingRequest cancelBooking(Long id) {
        BookingRequest booking = getBookingById(id);
        if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a booking that is in progress or completed");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Override
    public BookingRequest assignDriver(Long id, DriverAssignmentRequest request) {
        BookingRequest booking = getBookingById(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Driver can only be assigned to a CONFIRMED booking");
        }
        // Logic to assign driver and vehicle (you might need to update the BookingRequest entity to store driverId and vehicleId)
        // For now, we'll assume this is handled in additionalNotes or a future field
        return bookingRepository.save(booking);
    }

    @Override
    public List<BookingRequest> getProviderBookingsByStatus(Long providerId, BookingStatus status) {
        return bookingRepository.findByProviderIdAndStatus(providerId, status);
    }

    @Override
    public void contactParty(Long id, ContactRequest request) {
        BookingRequest booking = getBookingById(id);
        // Logic to initiate contact (e.g., send email, SMS, or in-app message)
        // This could involve integrating with a third-party service like Twilio for SMS or SendGrid for email
        // For now, we'll assume the contact logic is handled externally
        System.out.println("Contacting party for booking ID " + id + " via " + request.getContactMethod() + ": " + request.getMessage());
    }

    @Override
    public List<BookingRequest> getClientBookingHistory(Long clientId) {
        // History includes completed and cancelled bookings
        List<BookingStatus> historyStatuses = Arrays.asList(BookingStatus.COMPLETED, BookingStatus.CANCELLED);
        return bookingRepository.findByClientIdAndStatusIn(clientId, historyStatuses);
    }

    @Override
    public List<BookingRequest> getProviderBookingHistory(Long providerId) {
        // History includes completed and cancelled bookings
        List<BookingStatus> historyStatuses = Arrays.asList(BookingStatus.COMPLETED, BookingStatus.CANCELLED);
        return bookingRepository.findByProviderIdAndStatusIn(providerId, historyStatuses);
    }



    @Override
    public Page<BookingRequest> getPendingBookingsWithFilters(
            String pickupCountry, String pickupState, String pickupCity, String pickupStreet,
            String pickupLga, String pickupTown, String destinationCountry, String destinationState,
            String destinationCity, String destinationStreet, String destinationLga, String destinationTown,
            LocalDateTime serviceStartDate, LocalDateTime serviceEndDate,
            LocalDateTime createdStartDate, LocalDateTime createdEndDate, EnumPriority priority,
            Pageable pageable) {
        return bookingRepository.findByStatusAndFilters(
                BookingStatus.PENDING, pickupCountry, pickupState, pickupCity, pickupStreet, pickupLga, pickupTown,
                destinationCountry, destinationState, destinationCity, destinationStreet, destinationLga, destinationTown,
                serviceStartDate, serviceEndDate, createdStartDate, createdEndDate, priority, pageable);
    }

    @Override
    public BookingRequest acceptOffer(Long bookingId, Long offerId) {
        BookingRequest booking = getBookingById(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Cannot accept offer for non-PENDING booking");
        }

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));

        if (!offer.getBookingRequestId().equals(bookingId)) {
            throw new RuntimeException("Offer does not belong to this booking");
        }

        booking.setProviderId(offer.getProviderId());
        booking.setStatus(BookingStatus.CLAIMED); // Set to CLAIMED upon acceptance and payment
        return bookingRepository.save(booking);
    }

    @Override
    public List<BookingRequest> getAllBookings() {
        return bookingRepository.findAll();
    }
}