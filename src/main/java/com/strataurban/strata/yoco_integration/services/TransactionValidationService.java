package com.strataurban.strata.yoco_integration.services;

import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionValidationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferRepository offerRepository;

    public Boolean customerExists(Long id) {
        return userRepository.existsById(id);
    }

    public Boolean isCorrectOfferAmount(BigDecimal transactionAmount, String transactionReference) {
        Offer existingOffer = offerRepository.findByTransactionReference(transactionReference);
        return transactionAmount.compareTo(existingOffer.getPrice()) == 0;
    }

    public Boolean transactionExists(String transactionReference) {
        return offerRepository.existsByTransactionReference(transactionReference);
    }

    public Boolean isCorrectCurrency(String currencyCode, String transactionReference) {
        Offer offer = offerRepository.findByTransactionReference(transactionReference);
        return StringUtils.equalsIgnoreCase(currencyCode, offer.getCurrencyCode());
    }
}
