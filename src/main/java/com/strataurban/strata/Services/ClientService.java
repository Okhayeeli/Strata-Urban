package com.strataurban.strata.Services;

import com.strataurban.strata.DTOs.BookingRequestDTO;
import com.strataurban.strata.DTOs.RateRequestBodyDTO;
import com.strataurban.strata.DTOs.RequestBodyID;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Supplier;
import com.strataurban.strata.Entities.User;

public interface ClientService {

    public void registerUser(User user);
    public String updateUser();
    public String loginUser();
    public String logoutUser();
    public String resetPassword();

    public String initiateRequest(BookingRequestDTO bookingRequestDTO);
    public void cancelRequest(Long bookingId);
    public void rateService(RateRequestBodyDTO rateRequestBodyDTO);

    public Offer viewSupplierOffer(String offerId);
    public String viewRequestStatus(RequestBodyID requestBodyID);
    public Supplier viewSupplierDetails(Long supplierId);

    public String availableSuppliers();

    public String getAvailableSupplierVehicles();
    public String requestLocation();
}
