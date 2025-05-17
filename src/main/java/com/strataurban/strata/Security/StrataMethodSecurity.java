package com.strataurban.strata.Security;

import com.strataurban.strata.Repositories.v2.BookingRepository;
import org.springframework.stereotype.Component;

@Component("methodSecurity")
public class StrataMethodSecurity {

    private final BookingRepository bookingRepository;

    public StrataMethodSecurity(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public boolean isClientOwner(Long entityId, Long clientId) {
        return bookingRepository.existsByIdAndClientId(entityId, clientId);
    }

    public boolean isProviderOwner(Long entityId, Long providerId) {
        return bookingRepository.existsByIdAndProviderId(entityId, providerId);
    }

}
