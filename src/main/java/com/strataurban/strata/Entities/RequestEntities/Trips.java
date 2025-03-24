package com.strataurban.strata.Entities.RequestEntities;

import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.TripStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Table
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Trips {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    @Column
    private String eventType;
    @Column
    private Date eventDate;
    @Column
    private String eventTime;
    @Column
    private Long routeId;
    @Column
    private Long requestId;
    @Column
    private BigDecimal pricing;
    @Column
    private String contactInfo;
    @Column
    private String additionalNotes;
    @Column
    private Long bookingId;
    @Column
    private Long providerId;
    @Column
    private Long clientId;
    @Column
    private Long driverId;
    @Column
    private Long vehicleId;
    @Column
    private LocalDateTime startTime;
    @Column
    private LocalDateTime endTime;
    @Column
    private String additionalStops;

    // Helper method to append new stops to the existing comma-separated string
    public void appendStops(List<String> newStops) {
        if (newStops == null || newStops.isEmpty()) {
            return;
        }
        String newStopsString = String.join(",", newStops);
        if (this.additionalStops == null || this.additionalStops.isEmpty()) {
            this.additionalStops = newStopsString;
        } else {
            this.additionalStops = this.additionalStops + "," + newStopsString;
        }
    }
}
