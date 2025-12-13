package com.strataurban.strata.Security;

import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.TransportRepository;
import org.springframework.stereotype.Component;

@Component("methodSecurity")
public class StrataMethodSecurity {

    private final BookingRepository bookingRepository;
    private final TransportRepository transportRepository;

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
}
