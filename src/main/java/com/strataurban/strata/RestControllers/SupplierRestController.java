package com.strataurban.strata.RestControllers;

import com.strataurban.strata.DTOs.BookingRequestDTO;
import com.strataurban.strata.DTOs.OfferRequestDTO;
import com.strataurban.strata.DTOs.RequestBodyID;
import com.strataurban.strata.DTOs.SupplierIDDTO;
import com.strataurban.strata.Entities.Passengers.Booking;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Supplier;
import com.strataurban.strata.Services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/supplier")
public class SupplierRestController {

    @Autowired
    SupplierService supplierService;

    @PostMapping("/register")
    public Supplier registerSupplier(@RequestBody Supplier supplier){
        return supplierService.registerSupplier(supplier);
    }

    @PutMapping("/update")
    public Supplier updateSupplier(@RequestBody Supplier supplier){
        return supplierService.updateSupplier(supplier);
    }


    @GetMapping("/all-requests")
    public List<Booking> viewRequests(@RequestBody SupplierIDDTO supplierIDDTO){
        return supplierService.viewRequests(supplierIDDTO);
    }

    @GetMapping("/single-request")
    public Booking viewSingleRequest(@RequestBody SupplierIDDTO supplierIDDTO){
        return supplierService.viewSingleRequest(supplierIDDTO);
    }

    @PostMapping("/create-offer")
    public Offer createOffer(@RequestBody OfferRequestDTO offerRequestDTO){
        return supplierService.createOffer(offerRequestDTO);
    }

    @PutMapping("/accept-request")
    public Booking acceptBooking(@RequestBody RequestBodyID requestBodyID){
        return supplierService.acceptRequest(requestBodyID.getId());
    }
}
