package com.strataurban.strata.Entities.Passengers;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Table
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long supplierId;
    private Long clientId;
    private String currency;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private LocalDateTime returnTime;
    private String pickUpLocation;
    private String dropOffLocation;
    private String modeOfTransport; // taxi or bus
    private String requestType; //Book, Rent
    private String status; // pending, confirmed, etc.
    private Boolean isTwoWayTrip;
    private String additionalInformation;
    private BigDecimal price; //Price offered by the Supplier
    private BigDecimal priceOffer;
    private String rejectReason;
}
