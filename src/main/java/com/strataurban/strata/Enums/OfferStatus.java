package com.strataurban.strata.Enums;

public enum OfferStatus {
    ACCEPTED,               //Client has accepted the offer
    REJECTED,               //Client rejected the offer
    CANCELLED,              //Offer made invalid
    PENDING,                //Provider has made an offer and it is pending acceptance
    EXPIRED,                //Valid Time set by Provider has been exhausted
    PAID                    //Client has made payment for the Offer
}
