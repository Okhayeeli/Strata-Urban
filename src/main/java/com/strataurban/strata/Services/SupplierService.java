package com.strataurban.strata.Services;

import com.strataurban.strata.DTOs.OfferRequestDTO;
import com.strataurban.strata.DTOs.SupplierIDDTO;
import com.strataurban.strata.Entities.Passengers.Booking;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Supplier;

import java.util.List;

public interface SupplierService {

    public Supplier registerSupplier(Supplier supplier);
    public Supplier updateSupplier(Supplier supplier);
    public String loginSupplier();
    public String logoutSupplier();

    public List<Booking> viewRequests(SupplierIDDTO supplierIDDTO);
    public Booking viewSingleRequest(SupplierIDDTO supplierIDDTO);
    public Offer createOffer(OfferRequestDTO offerRequestDTO);
    public String viewOffer();
    public Booking acceptRequest(Long bookingId);
    public String requestMeeting();
    public String cancelOffer();
}
