package com.strataurban.strata.Entities.Passengers;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.DriverStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Driver entity - extends User
 * Represents drivers in the logistics system who are managed by Providers
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "driver")
@Entity
public class Driver extends User {

    @Column(name = "rating", columnDefinition = "decimal(3,2) default 0.00")
    private Double rating = 0.0;

    @Column(name = "number_of_ratings", columnDefinition = "int default 0")
    private Integer numberOfRatings = 0;

    @Column(name = "total_trips", columnDefinition = "int default 0")
    private Integer totalTrips = 0;

    @Column(name = "completed_trips", columnDefinition = "int default 0")
    private Integer completedTrips = 0;

    @Column(name = "cancelled_trips", columnDefinition = "int default 0")
    private Integer cancelledTrips = 0;

    @Column(name = "rejected_trips", columnDefinition = "int default 0")
    private Integer rejectedTrips = 0;


    // ==================== STATUS & AVAILABILITY ====================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "driver_status", columnDefinition = "varchar(20) default 'OFFLINE'")
    private DriverStatus driverStatus = DriverStatus.AVAILABLE; // AVAILABLE, BUSY, OFFLINE, SUSPENDED, ON_BREAK

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private boolean isActive = true; // Can be assigned trips

    @Column(name = "is_available", columnDefinition = "boolean default false")
    private boolean isAvailable = false; // Currently available for trips

    @Column(name = "is_on_trip", columnDefinition = "boolean default false")
    private boolean isOnTrip = false; // Currently on a trip

    @Column(name = "current_trip_id")
    private Long currentTripId;

    @Column(name = "last_active_date")
    private LocalDateTime lastActiveDate;

    @Column(name = "last_trip_completed_date")
    private LocalDateTime lastTripCompletedDate;

    @Column(name = "total_online_hours", columnDefinition = "decimal(10,2) default 0.00")
    private Double totalOnlineHours = 0.0;

    @Column(name = "online_today_hours", columnDefinition = "decimal(5,2) default 0.00")
    private Double onlineTodayHours = 0.0;

    // ==================== FINANCIAL INFORMATION ====================

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "tax_id_number")
    private String taxIdNumber;

    @Column(name = "total_earnings", columnDefinition = "decimal(12,2) default 0.00")
    private Double totalEarnings = 0.0;

    @Column(name = "pending_earnings", columnDefinition = "decimal(12,2) default 0.00")
    private Double pendingEarnings = 0.0;

    @Column(name = "withdrawn_earnings", columnDefinition = "decimal(12,2) default 0.00")
    private Double withdrawnEarnings = 0.0;



    // ==================== HELPER METHODS ====================

    /**
     * Check if driver is currently available for trip assignment
     */
    @Transient
    public boolean isReadyForTrip() {
        return isActive 
            && isAvailable 
            && !isOnTrip 
            && isEmailVerified()
            && driverStatus == DriverStatus.AVAILABLE
            && !isAccountLocked();
    }

    /**
     * Check if account is locked
     */
    @Transient
    public boolean isAccountLocked() {
        return getAccountLockedUntil() != null && getAccountLockedUntil().isAfter(LocalDateTime.now());
    }



    /**
     * Get full name
     */
    @Transient
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (getFirstName() != null) name.append(getFirstName());
        if (getMiddleName() != null) name.append(" ").append(getMiddleName());
        if (getLastName() != null) name.append(" ").append(getLastName());
        return name.toString().trim();
    }

    /**
     * Get rating display string
     */
    @Transient
    public String getRatingDisplay() {
        if (rating == null || numberOfRatings == null || numberOfRatings == 0) {
            return "No ratings yet";
        }
        return String.format("%.1f ⭐ (%d ratings)", rating, numberOfRatings);
    }

    /**
     * Calculate success rate (completed trips / total trips)
     */
    @Transient
    public Double getSuccessRate() {
        if (totalTrips == null || totalTrips == 0) {
            return 0.0;
        }
        return (completedTrips != null ? completedTrips : 0) * 100.0 / totalTrips;
    }

    /**
     * Update rating with new rating value
     */
    public void updateRating(double newRating) {
        if (this.rating == null || this.numberOfRatings == null) {
            this.rating = newRating;
            this.numberOfRatings = 1;
        } else {
            double totalRating = this.rating * this.numberOfRatings;
            this.numberOfRatings++;
            this.rating = (totalRating + newRating) / this.numberOfRatings;
        }
    }

    /**
     * Increment trip counters
     */
    public void incrementTotalTrips() {
        this.totalTrips = (this.totalTrips != null ? this.totalTrips : 0) + 1;
    }

    /**
     * Start a trip
     */
    public void startTrip(Long tripId) {
        this.isOnTrip = true;
        this.isAvailable = false;
        this.currentTripId = tripId;
        this.driverStatus = DriverStatus.BUSY;
        this.lastActiveDate = LocalDateTime.now();
    }

    /**
     * End a trip
     */
    public void endTrip() {
        this.isOnTrip = false;
        this.isAvailable = true;
        this.currentTripId = null;
        this.driverStatus = DriverStatus.AVAILABLE;
        this.lastActiveDate = LocalDateTime.now();
    }

    /**
     * Toggle availability
     */
    public void toggleAvailability() {
        this.isAvailable = !this.isAvailable;
        this.driverStatus = this.isAvailable ? DriverStatus.AVAILABLE : DriverStatus.OFFLINE;
        this.lastActiveDate = LocalDateTime.now();
    }

    /**
     * Go offline
     */
    public void goOffline() {
        this.isAvailable = false;
        this.driverStatus = DriverStatus.OFFLINE;
        this.lastActiveDate = LocalDateTime.now();
    }

    /**
     * Go online
     */
    public void goOnline() {
        if (isReadyForTrip()) {
            this.isAvailable = true;
            this.driverStatus = DriverStatus.AVAILABLE;
            this.lastActiveDate = LocalDateTime.now();
        }
    }
}
