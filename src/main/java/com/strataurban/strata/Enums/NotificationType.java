package com.strataurban.strata.Enums;

public enum NotificationType {
    BOOKING_REQUEST,        // New booking request for a Provider
    BOOKING_CONFIRMED,      // Booking confirmed for a Client
    BOOKING_CANCELLED,      // Booking cancelled (for both Client and Provider)
    TRIP_STARTED,           // Trip started (for Client)
    TRIP_ENDED,             // Trip ended (for Client)
    DRIVER_ASSIGNED,        // Driver assigned to a booking (for Client)
    NEW_MESSAGE,            // New in-app message (e.g., from "Contact" button)
    PAYMENT_SUCCESSFUL,     // Payment processed successfully
    PAYMENT_FAILED,         // Payment failed
    ACCOUNT_SUSPENDED,      // Account suspended (Admin action)
    RATING_REQUEST,         // Request to rate a Provider after a trip
    PAYMENT_RECEIVED,
    TRIP_COMPLETED,
}