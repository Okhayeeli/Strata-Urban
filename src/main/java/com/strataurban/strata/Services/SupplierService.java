//package com.strataurban.strata.Services;
//
//import com.strataurban.strata.DTOs.OfferRequestDTO;
//import com.strataurban.strata.DTOs.SupplierIDDTO;
//import com.strataurban.strata.Entities.Providers.Offer;
//import com.strataurban.strata.Entities.Providers.Provider;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//import java.util.List;
//
//public interface SupplierService {
//
//    public Provider registerSupplier(Provider provider);
//    public Provider updateSupplier(Provider provider);
//    public String loginSupplier();
//    public String logoutSupplier();
//
//    public Page<Provider> getAllSuppliers(Pageable pageable);
//    public List<Booking> viewRequests(SupplierIDDTO supplierIDDTO);
//    public Booking viewSingleRequest(SupplierIDDTO supplierIDDTO);
//    public Offer createOffer(OfferRequestDTO offerRequestDTO);
//    public String viewOffer();
//    public Booking acceptRequest(Long bookingId);
//    public String requestMeeting();
//    public String cancelOffer();
//}
