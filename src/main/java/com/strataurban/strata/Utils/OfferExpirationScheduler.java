package com.strataurban.strata.Utils;

import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Enums.OfferStatus;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OfferExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OfferExpirationScheduler.class);

    private final OfferRepository offerRepository;

    @Autowired
    public OfferExpirationScheduler(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredOffers() {
        logger.info("Checking for expired offers");
        List<Offer> pendingOffers = offerRepository.findAll().stream()
                .filter(offer -> offer.getStatus() == OfferStatus.PENDING)
                .filter(offer -> offer.getValidUntil() != null)
                .filter(offer -> offer.getValidUntil().isBefore(LocalDateTime.now()))
                .toList();

        for (Offer offer : pendingOffers) {
            offer.setStatus(OfferStatus.EXPIRED);
            offerRepository.save(offer);
            logger.info("Offer ID: {} has expired and status set to EXPIRED", offer.getId());
        }
    }
}