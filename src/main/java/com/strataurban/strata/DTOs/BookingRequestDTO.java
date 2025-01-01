package com.strataurban.strata.DTOs;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
public class BookingRequestDTO {

    private LocalDateTime departure;
    private LocalDateTime arrival;
    private LocalDateTime returnTime;
    private String dropOff;
    private String pickUp;
    private String modeOfTransport;
    private String requestType;
    private Boolean isTwoWay;
    private BigDecimal pricePerUnit;
    private String supplierId;
    private String transport;
    private int numberOfUnits;
    private String extraInformation;

}
