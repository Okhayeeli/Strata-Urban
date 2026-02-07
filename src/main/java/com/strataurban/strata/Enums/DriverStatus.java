package com.strataurban.strata.Enums;

/**
 * Driver Status Enum
 * Represents the current status/state of a driver
 */
public enum DriverStatus {
    AVAILABLE,      // Driver is online and available for trip assignments
    BUSY,           // Driver is currently on a trip
    OFFLINE,        // Driver is not available (logged out or off duty)
    ON_BREAK,       // Driver is taking a break
    SUSPENDED       // Driver account is suspended
}
