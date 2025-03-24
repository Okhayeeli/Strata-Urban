//package com.strataurban.strata.ServiceImpls;
//
//import com.strataurban.strata.DTOs.BookingRequestDTO;
//import com.strataurban.strata.DTOs.RateRequestBodyDTO;
//import com.strataurban.strata.DTOs.RequestBodyID;
//import com.strataurban.strata.Entities.Passengers.Booking;
//import com.strataurban.strata.Entities.Providers.Offer;
//import com.strataurban.strata.Entities.Providers.Provider;
//import com.strataurban.strata.Entities.User;
//
//import com.strataurban.strata.Repositories.OfferRepository;
//import com.strataurban.strata.Repositories.SupplierRepository;
//import com.strataurban.strata.Repositories.v2.UserRepository;
//import com.strataurban.strata.Repositories.v2.BookingRepository;
//import com.strataurban.strata.Services.ClientService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//
//import static com.strataurban.strata.Enums.Status.CANCELLED;
//import static com.strataurban.strata.Enums.Status.PENDING;
//import static com.strataurban.strata.Utils.MathUtils.findRatingAverage;
//
//@Service
//public class ClientServiceImpl implements ClientService {
//
//    @Autowired
//    BookingRepository bookingRepository;
//
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    SupplierRepository supplierRepository;
//    @Autowired
//    private OfferRepository offerRepository;
//
//    @Override
//    public void registerUser(User user) {
//        userRepository.save(user);
//    }
//
//    @Override
//    public String updateUser() {
//        return "";
//    }
//
//    @Override
//    public String loginUser() {
//        return "";
//    }
//
//    @Override
//    public String logoutUser() {
//        return "";
//    }
//
//    @Override
//    public String resetPassword() {
//        return "";
//    }
//
//
//
//
//    @Override
//    public String initiateRequest(BookingRequestDTO bookingRequestDTO) {
//
//        Booking booking = new Booking();
////        booking.setClientId(); //TODO AFTER AUTHENTICATION
//        booking.setDepartureTime(bookingRequestDTO.getDeparture());
//        booking.setArrivalTime(bookingRequestDTO.getArrival());
//        booking.setReturnTime(bookingRequestDTO.getReturnTime());
//        booking.setPickUpLocation(bookingRequestDTO.getPickUp());
//        booking.setDropOffLocation(bookingRequestDTO.getDropOff());
//        booking.setModeOfTransport(bookingRequestDTO.getModeOfTransport());
//        booking.setRequestType(bookingRequestDTO.getRequestType());
//        booking.setStatus(PENDING.name());
//        booking.setIsTwoWayTrip(bookingRequestDTO.getIsTwoWay() != null ?
//                bookingRequestDTO.getIsTwoWay() : false);
//
//        booking.setAdditionalInformation(bookingRequestDTO.getExtraInformation());
//
//        BigDecimal totalPrice = bookingRequestDTO.getPricePerUnit().multiply(
//                BigDecimal.valueOf(bookingRequestDTO.getNumberOfUnits()));
//        booking.setPrice(totalPrice);
//
////        bookingRepository.save(booking);
//
//        return "Successfully initiated";
//    }
//
//    @Override
//    public void cancelRequest(Long bookingId) {
////
////        Booking existingBooking = bookingRepository.findById
////                (bookingId).orElseThrow(()-> new RuntimeException("Booking wasn't found"));
////
////        existingBooking.setStatus(CANCELLED.name());
//    }
//
//    @Override
//    public void rateService(RateRequestBodyDTO rateRequestBodyDTO) {
//        Provider existingProvider = supplierRepository.findById(rateRequestBodyDTO.getId())
//                .orElseThrow(()-> new RuntimeException("Supplier wasn't found"));
//
//        existingProvider.setRating(
//        findRatingAverage(rateRequestBodyDTO.getRate(),
//                existingProvider.getRating(), existingProvider.getNumberOfRatings()));
//
//        existingProvider.setNumberOfRatings(existingProvider.getNumberOfRatings()+1);
//        supplierRepository.save(existingProvider);
//    }
//
//    @Override
//    public Offer viewSupplierOffer(String offerId) {
//        return offerRepository.findById(offerId)
//                .orElseThrow(()-> new RuntimeException("Offer not found"));
//    }
//
//    @Override
//    public String viewRequestStatus(RequestBodyID requestBodyID) {
////        Booking existingBooking = bookingRepository.findById
////                (requestBodyID.getId()).orElseThrow(()-> new RuntimeException("Booking wasn't found"));
////        return existingBooking.getStatus();
//        return null;
//    }
//
//    @Override
//    public Provider viewSupplierDetails(Long supplierId) {
//        return supplierRepository.findById(supplierId).orElseThrow(()-> new RuntimeException("Supplier wasn't found"));
//    }
//
//    @Override
//    public String availableSuppliers() {
//        return "";
//    }
//
//    @Override
//    public String getAvailableSupplierVehicles() {
//        return "";
//    }
//
//    @Override
//    public String requestLocation() {
//        return "";
//    }
//}
