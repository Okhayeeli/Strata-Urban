package com.strataurban.strata.RestControllers;

import com.strataurban.strata.DTOs.BookingRequestDTO;
import com.strataurban.strata.DTOs.RateRequestBodyDTO;
import com.strataurban.strata.DTOs.RequestBodyID;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Supplier;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/client")
public class ClientRestController {

    @Autowired
    ClientService clientService;

    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {
        clientService.registerUser(user);
        return "User registered successfully";
    }

    @PostMapping("/initiaterequest")
    public String initiateRequest(@RequestBody BookingRequestDTO bookingRequestDTO) {
        return clientService.initiateRequest(bookingRequestDTO);
    }

    @PutMapping("/cancelrequest")
    public String cancelRequest(@RequestBody RequestBodyID requestBodyID) {
        clientService.cancelRequest(requestBodyID.getId());
        return "Request cancelled successfully";
    }

    @PutMapping("/ratesupplier")
    public String rateSupplier(@RequestBody RateRequestBodyDTO rateRequestBodyDTO) {
        clientService.rateService(rateRequestBodyDTO);
        return "Thank you for your review";
    }

    @GetMapping("/viewstatus")
    public String viewStatus(@RequestBody RequestBodyID requestBodyID) {
        return clientService.viewRequestStatus(requestBodyID);
    }

    @GetMapping("/viewsupplier")
    public Supplier viewSupplier(@RequestParam Long supplierId){
        return clientService.viewSupplierDetails(supplierId);
    }

    @GetMapping("/view-offer")
    public Offer viewOffer(@RequestParam String offerId){
        return clientService.viewSupplierOffer(offerId);
    }
}
