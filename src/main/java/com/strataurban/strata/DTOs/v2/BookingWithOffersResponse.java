package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BookingWithOffersResponse {
    private BookingRequest booking;
    private List<Offer> offers;
}