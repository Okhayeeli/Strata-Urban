package com.strataurban.strata.Security;

import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Repositories.v2.TransportRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("methodSecurity")
public class StrataMethodSecurity {

    private final BookingRepository bookingRepository;
    private final TransportRepository transportRepository;
    private OfferRepository offerRepository;

    public StrataMethodSecurity(BookingRepository bookingRepository, TransportRepository transportRepository) {
        this.bookingRepository = bookingRepository;
        this.transportRepository = transportRepository;
    }

    public boolean isClientOwner(Long entityId, Long clientId) {
        return bookingRepository.existsByIdAndClientId(entityId, clientId);
    }

    public boolean isProviderOwner(Long entityId, Long providerId) {
        return bookingRepository.existsByIdAndProviderId(entityId, providerId);
    }

    public boolean isTransportProviderOwner(Long entityId, Long providerId) {
        return transportRepository.existsByIdAndProviderId(entityId, providerId);
    }

    public boolean isClientOffer(Long entityId, Long clientId) {
        return Objects.equals(entityId, clientId);
    }
}
