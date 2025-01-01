package com.strataurban.strata.ServiceImpls;

import com.strataurban.strata.DTOs.OfferRequestDTO;
import com.strataurban.strata.DTOs.SupplierIDDTO;
import com.strataurban.strata.Entities.Passengers.Booking;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Supplier;
import com.strataurban.strata.Repositories.BookingRepository;
import com.strataurban.strata.Repositories.OfferRepository;
import com.strataurban.strata.Repositories.SupplierRepository;
import com.strataurban.strata.Repositories.UserRepository;
import com.strataurban.strata.Services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.strataurban.strata.Enums.Status.ACCEPTED;
import static com.strataurban.strata.Enums.Status.PENDING;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    OfferRepository offerRepository;


    @Override
    public Supplier registerSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier updateSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Override
    public String loginSupplier() {
        return "";
    }

    @Override
    public String logoutSupplier() {
        return "";
    }

    @Override
    public List<Booking> viewRequests(SupplierIDDTO supplierIDDTO) {
        return bookingRepository.findAllBySupplierId(supplierIDDTO.getSupplierId());

    }

    @Override
    public Booking viewSingleRequest(SupplierIDDTO supplierIDDTO) {
        return bookingRepository.findById(supplierIDDTO.getBookingId())
                .orElseThrow(()-> new RuntimeException("Booking wasn't found"));
    }

    @Override
    public Offer createOffer(OfferRequestDTO offerRequestDTO) {

        Offer offer = new Offer();
        offer.setNumberOfVehicles(offer.getNumberOfVehicles());
        offer.setPricePerUnit(offerRequestDTO.getPricePerUnit());

        BigDecimal totalPrice = offerRequestDTO.getPricePerUnit()
                .multiply(offerRequestDTO.getNumberOfVehicles());

        offer.setTotalPrice(totalPrice);
        offer.setVehicleType(offerRequestDTO.getVehicleType());
        offer.setModel(offerRequestDTO.getModel());
        offer.setBrand(offerRequestDTO.getBrand());
        offer.setCapacity(offerRequestDTO.getCapacity());
        offer.setFuelType(offerRequestDTO.getFuelType());
        offer.setTransmissionType(offerRequestDTO.getTransmissionType());
        offer.setVehicleColor(offerRequestDTO.getVehicleColor());
        offer.setVehicleAge(offerRequestDTO.getVehicleAge());
        offer.setLicensePlate(offerRequestDTO.getLicensePlate());
        offer.setClientId(offerRequestDTO.getClientId());
//        offer.setSupplierId(); //TODO After Authentication
//        offer.setSupplierName(); //TODO After Authentication
//        offer.setSupplierRating(); //TODO After Authentication
        offer.setOfferStatus(PENDING);
        offer.setOfferDate(LocalDateTime.now());
        offer.setExpiryDate(offerRequestDTO.getExpiryDate());
        offer.setDiscount(offerRequestDTO.getDiscount());
        offer.setCurrency(offerRequestDTO.getCurrency());
        offer.setPickupLocation(offerRequestDTO.getPickupLocation());
        offer.setDropOffLocation(offerRequestDTO.getDropOffLocation());
        offer.setAvailabilityStart(offerRequestDTO.getAvailabilityStart());
        offer.setAvailabilityEnd(offerRequestDTO.getAvailabilityEnd());
        offer.setNotes(offerRequestDTO.getNotes());

        offerRepository.save(offer);

        return offerRepository.save(offer);
    }

    @Override
    public String viewOffer() {
        return "";
    }

    @Override
    public Booking acceptRequest(Long bookingId) {

        Booking existingBooking = bookingRepository.findById
                (bookingId).orElseThrow(()-> new RuntimeException("Booking wasn't found"));

        existingBooking.setStatus(ACCEPTED.name());
        return existingBooking;
    }

    @Override
    public String requestMeeting() {
        return "";
    }

    @Override
    public String cancelOffer() {
        return "";
    }
}
