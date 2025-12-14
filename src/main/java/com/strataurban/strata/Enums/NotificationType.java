package com.strataurban.strata.Enums;

public enum NotificationType {

    BOOKING_REQUEST,        // New booking request sent to a Provider
    BOOKING_CONFIRMED,      // Booking confirmed and sent to the Client
    BOOKING_CANCELLED,      // Booking cancelled (sent to both Client and Provider)

    OFFER_RECEIVED,         // Offer received by a Client from a Provider
    OFFER_REJECTED,         // Offer rejected by a Client (sent to the Provider)
    OFFER_ACCEPTED,         // Offer accepted by a Client (sent to the Provider)

    TRIP_STARTED,           // Trip has started (sent to the Client)
    TRIP_ENDED,             // Trip has ended (sent to the Client)
    TRIP_COMPLETED,         // Trip fully completed and closed (sent to Client and/or Provider)

    DRIVER_ASSIGNED,        // Driver assigned to a booking (sent to the Client)
    NEW_MESSAGE,            // New in-app message (e.g., via Contact or Chat feature)

    PAYMENT_SUCCESSFUL,     // Payment processed successfully (sent to Client)
    PAYMENT_FAILED,         // Payment attempt failed (sent to Client)
    PAYMENT_RECEIVED,       // Payment received and confirmed (sent to Provider)

    ACCOUNT_SUSPENDED,      // Account suspended by Admin action
    RATING_REQUEST          // Request sent to Client to rate the Provider after trip
}
