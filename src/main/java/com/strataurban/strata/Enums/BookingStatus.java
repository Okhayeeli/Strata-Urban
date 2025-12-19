package com.strataurban.strata.Enums;

public enum BookingStatus {

    PENDING,                //Booking awaiting offer on it
    IN_PROGRESS,            //Offer made, waiting for acceptance or rejection
    CONFIRMED,              //This is supposed to be done after payment has been done and the Provider confirms receipt of the payment
    COMPLETED,              //Trip Completed
    CLAIMED,                //Booking claimed by Provider
    CANCELLED
}
