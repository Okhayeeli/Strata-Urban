package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.Entities.Passengers.Client;
import com.strataurban.strata.Entities.Passengers.Receipt;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.Routes;
import com.strataurban.strata.Entities.Providers.Transport;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Repositories.v2.*;
import com.strataurban.strata.yoco_integration.entities.PaymentTransaction;
import com.strataurban.strata.yoco_integration.repositories.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BookingRepository bookingRequestRepository;
    private final OfferRepository offerRepository;
    private final ClientRepository clientRepository;
    private final ProviderRepository providerRepository;
    private final TransportRepository transportRepository;
    private final RouteRepository routesRepository;

    /**
     * Generate receipt after successful payment
     */
    @Async
    public void generateReceipt(Long paymentTransaction, Long aNumber) {
        generateReceipt(paymentTransaction);
    }


    public Receipt generateReceipt(Long paymentTransactionId) {
        // Check if receipt already exists
        if (receiptRepository.existsByPaymentTransactionId(paymentTransactionId)) {
            log.info("Receipt already exists for payment transaction: {}", paymentTransactionId);
            return receiptRepository.findByPaymentTransactionId(paymentTransactionId)
                    .orElseThrow(() -> new RuntimeException("Receipt lookup failed"));
        }

        // Fetch payment transaction
        PaymentTransaction payment = paymentTransactionRepository.findById(paymentTransactionId)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found: " + paymentTransactionId));

        // Verify payment is successful
        if (payment.getStatus() != PaymentTransaction.PaymentStatus.SUCCEEDED) {
            throw new RuntimeException("Cannot generate receipt for non-successful payment");
        }

        // Fetch booking
        BookingRequest booking = bookingRequestRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found: " + payment.getBookingId()));

        // Fetch offer
        Offer offer = offerRepository.findById(Long.parseLong(booking.getOfferIds().split(",")[0]))
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        // Fetch client
        Client client = clientRepository.findById(booking.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found: " + booking.getClientId()));

        // Fetch provider
        Provider provider = providerRepository.findById(booking.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found: " + booking.getProviderId()));

        // Fetch vehicle and route (optional)
        Transport vehicle = null;
        if (booking.getVehicleId() != null) {
            vehicle = transportRepository.findById(booking.getVehicleId()).orElse(null);
        }

        Routes route = null;
        if (booking.getRouteId() != null) {
            route = routesRepository.findById(booking.getRouteId()).orElse(null);
        }

        // Build receipt
        Receipt receipt = Receipt.builder()
                // Payment details
                .paymentTransactionId(payment.getId())
                .checkoutId(payment.getCheckoutId())
                .amountPaid(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentDate(payment.getCompletedAt() != null ? payment.getCompletedAt() : payment.getUpdatedAt())
                .paymentMethod("Online Payment") // You can enhance this based on actual payment method

                // Booking details
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .offerId(offer.getId())
                .offerTransactionReference(offer.getTransactionReference())

                // Client details
                .clientId(client.getId())
                .clientName(buildFullName(client.getFirstName(), client.getMiddleName(), client.getLastName()))
                .clientEmail(client.getEmail())
                .clientPhone(client.getPhone())

                // Provider details
                .providerId(provider.getId())
                .providerName(provider.getCompanyName() != null ? provider.getCompanyName() :
                        buildFullName(provider.getFirstName(), provider.getMiddleName(), provider.getLastName()))
                .providerEmail(provider.getCompanyBusinessEmail() != null ?
                        provider.getCompanyBusinessEmail() : provider.getEmail())
                .providerPhone(provider.getCompanyBusinessPhone() != null ?
                        provider.getCompanyBusinessPhone() : provider.getPhone())
                .providerAddress(provider.getCompanyAddress() != null ?
                        provider.getCompanyAddress() : provider.getAddress())

                // Service details
                .serviceDescription(buildServiceDescription(booking))
                .serviceDate(booking.getServiceDate())
                .pickUpLocation(booking.getPickUpLocation())
                .destination(booking.getDestination())
                .routeDescription(route != null ? route.getStart() + " → " + route.getEnd() : null)

                // Vehicle & driver
                .vehicleId(booking.getVehicleId())
                .vehicleDetails(vehicle != null ? buildVehicleDetails(vehicle) : null)
                .driverId(booking.getDriverId())
                .driverName(null) // You'd need to fetch driver details if you have a Driver entity

                // Pricing breakdown
                .originalPrice(offer.getPrice())
                .discountPercentage(offer.getDiscountPercentage())
                .discountAmount(calculateDiscountAmount(offer.getPrice(), offer.getDiscountPercentage()))
                .subtotal(offer.getDiscountedPrice())
                .taxAmount(BigDecimal.ZERO) // Calculate if applicable
                .serviceFee(BigDecimal.ZERO) // Calculate if applicable
                .totalAmount(payment.getAmount())

                // Additional info
                .numberOfPassengers(booking.getNumberOfPassengers())
                .hasMultipleStops(booking.getHasMultipleStops())
                .isReturnTrip(booking.getIsReturnTrip())
                .additionalNotes(booking.getAdditionalNotes())
                .specialConditions(offer.getSpecialConditions())
                .notes(offer.getNotes())

                .build();

        // Save and return
        Receipt savedReceipt = receiptRepository.save(receipt);
        log.info("Receipt generated successfully: {}", savedReceipt.getReceiptNumber());
        return savedReceipt;
    }

    /**
     * Get receipt by receipt number
     */
    public Optional<Receipt> getReceiptByNumber(String receiptNumber) {
        return receiptRepository.findByReceiptNumber(receiptNumber);
    }

    /**
     * Get receipt by payment transaction ID
     */
    public Optional<Receipt> getReceiptByPaymentId(Long paymentTransactionId) {
        return receiptRepository.findByPaymentTransactionId(paymentTransactionId);
    }

    /**
     * Get receipt by booking ID
     */
    public Optional<Receipt> getReceiptByBookingId(Long bookingId) {
        return receiptRepository.findByBookingId(bookingId);
    }

    /**
     * Get all receipts for a client
     */
    public List<Receipt> getClientReceipts(Long clientId) {
        return receiptRepository.findByClientId(clientId);
    }

    /**
     * Get all receipts for a provider
     */
    public List<Receipt> getProviderReceipts(Long providerId) {
        return receiptRepository.findByProviderId(providerId);
    }

    /**
     * Get receipts by date range
     */
    public List<Receipt> getReceiptsByDateRange(Long userId, boolean isClient,
                                                LocalDateTime startDate, LocalDateTime endDate) {
        if (isClient) {
            return receiptRepository.findByClientIdAndGeneratedAtBetween(userId, startDate, endDate);
        } else {
            return receiptRepository.findByProviderIdAndGeneratedAtBetween(userId, startDate, endDate);
        }
    }

    /**
     * Void a receipt (for refunds)
     */
    @Transactional
    public Receipt voidReceipt(String receiptNumber, String reason) {
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new RuntimeException("Receipt not found: " + receiptNumber));

        receipt.setStatus("VOIDED");
        receipt.setNotes((receipt.getNotes() != null ? receipt.getNotes() + " | " : "") +
                "Voided: " + reason);

        return receiptRepository.save(receipt);
    }

    // Helper methods
    private String buildFullName(String firstName, String middleName, String lastName) {
        StringBuilder name = new StringBuilder();
        if (firstName != null) name.append(firstName);
        if (middleName != null && !middleName.isEmpty()) name.append(" ").append(middleName);
        if (lastName != null) name.append(" ").append(lastName);
        return name.toString().trim();
    }

    private String buildServiceDescription(BookingRequest booking) {
        StringBuilder desc = new StringBuilder();

        if (Boolean.TRUE.equals(booking.getIsPassenger())) {
            desc.append("Passenger Transport");
            if (booking.getVehiclePreferenceType() != null) {
                desc.append(" - ").append(booking.getVehiclePreferenceType());
            }
        } else if (Boolean.TRUE.equals(booking.getIsCargo())) {
            desc.append("Cargo Transport - ").append(booking.getSupplyType());
        } else if (Boolean.TRUE.equals(booking.getIsMedical())) {
            desc.append("Medical Transport - ").append(booking.getMedicalItemType());
        } else if (Boolean.TRUE.equals(booking.getIsFurniture())) {
            desc.append("Furniture Transport - ").append(booking.getFurnitureType());
        } else if (Boolean.TRUE.equals(booking.getIsFood())) {
            desc.append("Food Delivery - ").append(booking.getFoodType());
        } else if (Boolean.TRUE.equals(booking.getIsEquipment())) {
            desc.append("Equipment Transport");
        } else {
            desc.append("Transport Service");
        }

        return desc.toString();
    }

    private String buildVehicleDetails(Transport vehicle) {
        return String.format("%s %s (%s) - %s",
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getPlateNumber());
    }

    private BigDecimal calculateDiscountAmount(BigDecimal originalPrice, Double discountPercentage) {
        if (discountPercentage == null || discountPercentage == 0) {
            return BigDecimal.ZERO;
        }
        return originalPrice.multiply(BigDecimal.valueOf(discountPercentage / 100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}