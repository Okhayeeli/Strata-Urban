package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Entities.Providers.Transport;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.OfferRepository;
import com.strataurban.strata.Repositories.v2.TransportRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.Security.SecurityUserDetailsService;
import com.strataurban.strata.Services.v2.BookingService;
import com.strataurban.strata.Services.v2.OfferService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.strataurban.strata.Enums.OfferStatus.EXPIRED;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private final BookingRepository bookingRepository;

    @Autowired
    private final OfferRepository offerRepository;

    @Autowired
    private final OfferService offerService;

    @Autowired
    private final UserRepository userRepository;

    private final SecurityUserDetailsService securityUserDetailsService;

    private final TransportRepository transportRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, OfferRepository offerRepository, OfferService offerService, UserRepository userRepository, SecurityUserDetailsService securityUserDetailsService, TransportRepository transportRepository) {
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.offerService = offerService;
        this.userRepository = userRepository;
        this.securityUserDetailsService = securityUserDetailsService;
        this.transportRepository = transportRepository;
    }

    @Override
    public BookingRequest createBooking(BookingRequestRequestDTO bookingRequest, Long clientId) {
        // Set the client ID (assuming BookingRequest has a clientId field)
        // Note: You might need to add a clientId field to the BookingRequest entity
        // For now, we'll assume the clientId is part of additionalNotes or metadata

        BookingRequest booking = mapToEntity(bookingRequest);
        booking.setClientId(clientId);
        SecurityUserDetails userDetails = securityUserDetailsService.getSecurityUserDetails();
        booking.setCity(userDetails.getCity());
        booking.setState(userDetails.getState());
        booking.setCountry(userDetails.getCountry());

        //TODO After Authentication
        booking.setStatus(BookingStatus.PENDING); // Default status
        return bookingRepository.save(booking);
    }

    @Override
    public Page<BookingRequest> getClientBookings(Long clientId, Pageable pageable) {
        return bookingRepository.findByClientId(clientId, pageable);
    }

    @Override
    public Page<BookingRequestResponseDTO> getProviderBookings(Long providerId, Pageable pageable) {
       Page<BookingRequest> bookingRequests = bookingRepository.findByProviderId(providerId, pageable);
       return mapPageToPageResponse(bookingRequests);
    }

    @Override
    public BookingRequest getBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
    }

    @Override
    public BookingRequest updateBookingStatus(Long id, BookingStatus status) {
        BookingRequest booking = getBookingById(id);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    @Override
    public BookingRequest confirmBooking(Long id) {
        BookingRequest booking = getBookingById(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Override
    public BookingRequest claimBooking(Long id, Long providerId) {
        BookingRequest booking = getBookingById(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CLAIMED);
        //TODO After Authentication, use the Provider Id
        booking.setProviderId(providerId);
        return bookingRepository.save(booking);
    }


    @Override
    public BookingRequest cancelBooking(Long id) {
        BookingRequest booking = getBookingById(id);
        if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a booking that is in progress or completed");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }


    @Override
    public BookingRequest assignDriver(Long id, DriverAssignmentRequest request) {
        // Fetch booking
        BookingRequest booking = getBookingById(id);

        // Check booking status
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.CLAIMED) {
            throw new IllegalStateException("Driver and vehicle can only be assigned to a CONFIRMED or CLAIMED booking");
        }

        // Fetch driver
        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + request.getDriverId()));

        // Validate driver role
        if (driver.getRoles() != EnumRoles.DRIVER) {
            throw new IllegalArgumentException("User with ID " + request.getDriverId() + " is not a DRIVER");
        }

        // Fetch vehicle
        Transport vehicle = transportRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + request.getVehicleId()));

        // Validate vehicle status
        if (!"Available".equals(vehicle.getStatus())) {
            throw new IllegalStateException("Vehicle with ID " + request.getVehicleId() + " is not Available");
        }

        // Get authenticated user
        SecurityUserDetails userDetails = securityUserDetailsService.getSecurityUserDetails();

        // Validate provider ID for driver and vehicle (for PROVIDER role)
        if (userDetails.getRole() == EnumRoles.PROVIDER) {
            if (driver.getProviderId() != null && !driver.getProviderId().equals(String.valueOf(userDetails.getId()))) {
                throw new SecurityException("Driver does not belong to this provider");
            }
            if (!vehicle.getProviderId().equals(userDetails.getId())) {
                throw new SecurityException("Vehicle does not belong to this provider");
            }
        }
        // Assign driver and vehicle to booking
        booking.setDriverId(request.getDriverId());
        booking.setVehicleId(request.getVehicleId());
        booking.setStatus(BookingStatus.CONFIRMED);
        vehicle.setStatus("Booked");
        transportRepository.save(vehicle);

        // Save and return booking
        return bookingRepository.save(booking);
    }


    @Override
    public Page<BookingRequestResponseDTO> getProviderBookingsByStatus(Long providerId, BookingStatus status, Pageable pageable) {
        return mapPageToPageResponse(bookingRepository.findByProviderIdAndStatus(providerId, status, pageable));
    }

    @Override
    public void contactParty(Long id, ContactRequest request) {
        BookingRequest booking = getBookingById(id);
        // Logic to initiate contact (e.g., send email, SMS, or in-app message)
        // This could involve integrating with a third-party service like Twilio for SMS or SendGrid for email
        // For now, we'll assume the contact logic is handled externally
        System.out.println("Contacting party for booking ID " + id + " via " + request.getContactMethod() + ": " + request.getMessage());
    }

    @Override
    public Page<BookingRequestResponseDTO> getClientBookingHistory(Long clientId, Pageable pageable) {
        // History includes completed and cancelled bookings
        List<BookingStatus> historyStatuses = Arrays.asList(BookingStatus.COMPLETED, BookingStatus.CANCELLED);
        Page<BookingRequest> bookingPage =  bookingRepository.findByClientIdAndStatusIn(clientId, historyStatuses, pageable);
        return bookingPage.map(this::mapToResponseDTO);
    }

    public Page<BookingRequestResponseDTO> mapToPageResponse(List<BookingRequest> bookingList, Pageable pageable) {
        // Map the list of BookingRequest to BookingRequestResponseDTO
        List<BookingRequestResponseDTO> dtoList = bookingList.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        // Create a PageImpl with the mapped DTOs
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageSize), dtoList.size());

        // Handle empty list or out-of-bounds cases
        List<BookingRequestResponseDTO> pageContent = (start < dtoList.size())
                ? dtoList.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, dtoList.size());
    }


    @Override
    public List<BookingRequest> getProviderBookingHistory(Long providerId) {
        // History includes completed and cancelled bookings
        List<BookingStatus> historyStatuses = Arrays.asList(BookingStatus.COMPLETED, BookingStatus.CANCELLED);
        return bookingRepository.findByProviderIdAndStatusIn(providerId, historyStatuses);
    }


    @Override
    public Page<BookingRequest> getPendingBookingsWithFilters(
            String pickUpLocation, String destination, String additionalStops,
            LocalDateTime serviceStartDate, LocalDateTime serviceEndDate,
            LocalDateTime pickupStartDateTime, LocalDateTime pickupEndDateTime,
            LocalDateTime createdStartDate, LocalDateTime createdEndDate,
            EnumPriority priority, Boolean isPassenger, Integer numberOfPassengers,
            String eventType, Boolean isCargo, Double estimatedWeightKg, String supplyType,
            Boolean isMedical, String medicalItemType, Boolean isFurniture, String furnitureType,
            Boolean isFood, String foodType, Boolean isEquipment, String equipmentItem, String city, String state, String country,
            Pageable pageable) {

        SecurityUserDetails userDetails = securityUserDetailsService.getSecurityUserDetails();
        // Check if the user is a provider
        if (userDetails.getRole() == EnumRoles.PROVIDER) {
            country = userDetails.getCountry();
        }

        return bookingRepository.findByStatusAndFilters(
                BookingStatus.PENDING, pickUpLocation, destination, additionalStops,
                serviceStartDate, serviceEndDate, pickupStartDateTime, pickupEndDateTime,
                createdStartDate, createdEndDate, priority,
                isPassenger, numberOfPassengers, eventType,
                isCargo, estimatedWeightKg, supplyType,
                isMedical, medicalItemType,
                isFurniture, furnitureType,
                isFood, foodType,
                isEquipment, equipmentItem, city, state, country,
                pageable);
    }

    @Override
    @Transactional
    public BookingRequest acceptOffer(Long bookingId, Long offerId) {
        log.info("Accepting offer ID: {} for booking ID: {}", offerId, bookingId);
        BookingRequest booking = getBookingById(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Cannot accept offer for non-PENDING booking");
        }

        String offerIds = booking.getOfferIds();
        if (offerIds == null || !Arrays.asList(offerIds.split(",")).contains(String.valueOf(offerId))) {
            throw new RuntimeException("Offer ID: " + offerId + " is not associated with booking ID: " + bookingId);
        }

        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new RuntimeException("Offer not found with ID: " + offerId));

        // Check if offer is expired
        if (offer.getStatus() == EXPIRED) {
            throw new RuntimeException("Offer ID: " + offerId + " has expired");
        }
        if (offer.getValidUntil() != null && offer.getValidUntil().isBefore(LocalDateTime.now())) {
            offer.setStatus(EXPIRED);
            offerRepository.save(offer);
            throw new RuntimeException("Offer ID: " + offerId + " has expired");
        }

        // Reject other offers
        offerService.disableOtherOffers(bookingId, offerId);

        // Update booking
        booking.setProviderId(offer.getProviderId());
        booking.setStatus(BookingStatus.CLAIMED);
        BookingRequest updatedBooking = bookingRepository.save(booking);
        log.info("Accepted offer for booking: {}", updatedBooking);
        return updatedBooking;
    }

    public Long getClientIdByBookingId(Long bookingId) {
        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));
        return booking.getClientId();
    }


    public BookingRequest mapToEntity(BookingRequestRequestDTO dto) {
        BookingRequest entity = new BookingRequest();

        // Map request_details
        BookingRequestRequestDTO.RequestDetails requestDetails = dto.getBookingRequest().getRequestDetails();
        entity.setServiceDate(requestDetails.getServiceDate());
        entity.setPickupDateTime(requestDetails.getPickUpDateTime());
        entity.setDropOffDateTime(requestDetails.getDropOffDateTime());
        entity.setTimingFlexible(requestDetails.getTimingFlexible());
        entity.setIsReturnTrip(requestDetails.getIsReturnTrip());
        entity.setAdditionalNotes(requestDetails.getAdditionalNotes());
        entity.setPriority(requestDetails.getUrgencyLevel() != null ? EnumPriority.valueOf(requestDetails.getUrgencyLevel()) : null);

        // Map locations
        BookingRequestRequestDTO.Locations locations = dto.getBookingRequest().getLocations();
        entity.setPickUpLocation(locations.getPickUpLocation());
        entity.setDestination(locations.getDestination());
        entity.setHasMultipleStops(locations.getHasMultipleStops());
        entity.setAdditionalStops(locations.getAdditionalStops() != null ? String.join(",", locations.getAdditionalStops()) : null);

        // Map contact_information (assuming stored separately or derived from clientId)
        BookingRequestRequestDTO.ContactInformation contactInfo = dto.getBookingRequest().getContactInformation();
        // Assuming clientId is set elsewhere based on authentication
        // entity.setClientId(fetchClientIdFromContactInfo(contactInfo));

        // Map category_specific_details
        BookingRequestRequestDTO.CategorySpecificDetails categoryDetails = dto.getBookingRequest().getCategorySpecificDetails();

        // Passenger Details
        if (categoryDetails.getPassengerDetails() != null) {
            entity.setIsPassenger(true);
            BookingRequestRequestDTO.PassengerDetails passenger = categoryDetails.getPassengerDetails();
            entity.setEventType(passenger.getEventType());
            entity.setNumberOfPassengers(passenger.getNumberOfPassengers());
            entity.setLuggageNeeded(passenger.getLuggageNeeded());
            entity.setLuggageDetails(passenger.getLuggageDetails());
            entity.setVehiclePreferenceType(passenger.getVehiclePreferenceType());

            // Special Requests
            if (passenger.getSpecialRequests() != null) {
                entity.setExtraAmenitiesRequired(true);
//                entity.setAmenities(passenger.getSpecialRequests().getAmenities());
                entity.setOtherRequests(passenger.getSpecialRequests().getOtherRequests());
                // Set individual amenities flags if needed
                if (passenger.getSpecialRequests().getAmenities() != null) {
                    entity.setAirConditioningRequired(passenger.getSpecialRequests().getAmenities().contains("Air conditioning"));
                    entity.setWiFiRequired(passenger.getSpecialRequests().getAmenities().contains("Wi-Fi"));
                    entity.setWheelChairAccessibility(passenger.getSpecialRequests().getAmenities().contains("Wheelchair accessibility"));
                    entity.setPowerOutletsRequired(passenger.getSpecialRequests().getAmenities().contains("Power outlets"));
                    entity.setMusicAndSoundSystemsRequired(passenger.getSpecialRequests().getAmenities().contains("Music and sound systems"));
                    entity.setTintedWindowsRequired(passenger.getSpecialRequests().getAmenities().contains("Tinted windows"));
                }
            } else {
                entity.setExtraAmenitiesRequired(false);
            }

            // Waiting Time Details
            if (passenger.getWaitingTimeDetails() != null) {
                entity.setWaitOnSite(passenger.getWaitingTimeDetails().getWaitOnSite());
                entity.setAdditionalStopsDetails(passenger.getWaitingTimeDetails().getAdditionalStopsDetails());
            }

            // Budget and Pricing
            if (passenger.getBudgetAndPricing() != null) {
                entity.setBudgetRange(passenger.getBudgetAndPricing().getBudgetRange());
            }

            // Quote liamentation Preferences
            if (passenger.getQuoteComparisonPreferences() != null) {
                entity.setPreferredFeatures(passenger.getQuoteComparisonPreferences().getPreferredFeatures() != null ? String.join(",", passenger.getQuoteComparisonPreferences().getPreferredFeatures()) : null);
            }
        } else {
            entity.setIsPassenger(false);
        }

        // Cargo Details
        if (categoryDetails.getCargoDetails() != null) {
            entity.setIsCargo(true);
            BookingRequestRequestDTO.CargoDetails cargo = categoryDetails.getCargoDetails();
            entity.setSupplyType(cargo.getSupplyType());
            entity.setEstimatedWeightKg(cargo.getEstimatedWeightKg());
            entity.setVolumeCubicMeters(cargo.getVolumeCubicMeters());
            entity.setPackageSize(cargo.getPackageSize());
            entity.setMaterialType(cargo.getMaterialType());
            entity.setNeedsFragileHandling(cargo.getNeedsFragileHandling());
            entity.setEstimatedValue(cargo.getEstimatedValue());
            entity.setHandlingInstruction(cargo.getHandlingInstruction());
            entity.setOffloadingHelpRequired(cargo.getOffloadingHelpRequired());
        } else {
            entity.setIsCargo(false);
        }

        // Medical Details
        if (categoryDetails.getMedicalDetails() != null) {
            entity.setIsMedical(true);
            BookingRequestRequestDTO.MedicalDetails medical = categoryDetails.getMedicalDetails();
            entity.setMedicalItemType(medical.getMedicalItemType());
            entity.setRefrigerationRequiredMedical(medical.getRefrigerationRequired());
            entity.setNeedsFragileHandlingMedical(medical.getNeedsFragileHandling());
        } else {
            entity.setIsMedical(false);
        }

        // Furniture Details
        if (categoryDetails.getFurnitureDetails() != null) {
            entity.setIsFurniture(true);
            BookingRequestRequestDTO.FurnitureDetails furniture = categoryDetails.getFurnitureDetails();
            entity.setFurnitureType(furniture.getFurnitureType());
            entity.setRequiresDisassembly(furniture.getRequiresDisassembly());
            entity.setHandlingInstructionFurniture(furniture.getHandlingInstruction());
            entity.setOffloadingHelpRequiredFurniture(furniture.getOffloadingHelpRequired());
        } else {
            entity.setIsFurniture(false);
        }

        // Food Details
        if (categoryDetails.getFoodDetails() != null) {
            entity.setIsFood(true);
            BookingRequestRequestDTO.FoodDetails food = categoryDetails.getFoodDetails();
            entity.setFoodType(food.getFoodType());
            entity.setRequiresHotBox(food.getRequiresHotBox());
            entity.setRefrigerationRequiredFood(food.getRefrigerationRequired());
            entity.setDietaryRestriction(food.getDietaryRestriction());
            entity.setEstimatedWeightKgFood(food.getEstimatedWeightKg());
            entity.setPackageSizeFood(food.getPackageSize());
        } else {
            entity.setIsFood(false);
        }

        // Equipment Details
        if (categoryDetails.getEquipmentDetails() != null) {
            entity.setIsEquipment(true);
            BookingRequestRequestDTO.EquipmentDetails equipment = categoryDetails.getEquipmentDetails();
            entity.setEquipmentList(equipment.getEquipmentList());
            entity.setSetupRequired(equipment.getSetupRequired());
        } else {
            entity.setIsEquipment(false);
        }

        return entity;
    }


    @Override
    public List<BookingRequest> getAllBookings() {
        return bookingRepository.findAll();
    }




    @Override
    public BookingRequestResponseDTO mapToResponseDTO(BookingRequest entity) {
            BookingRequestResponseDTO dto = new BookingRequestResponseDTO();
            BookingRequestResponseDTO.BookingRequestDetails bookingDetails = new BookingRequestResponseDTO.BookingRequestDetails();
            BookingRequestResponseDTO.RequestDetails requestDetails = new BookingRequestResponseDTO.RequestDetails();
            BookingRequestResponseDTO.Locations locations = new BookingRequestResponseDTO.Locations();
            BookingRequestResponseDTO.ContactInformation contactInfo = new BookingRequestResponseDTO.ContactInformation();
            BookingRequestResponseDTO.CategorySpecificDetails categoryDetails = new BookingRequestResponseDTO.CategorySpecificDetails();

            // Map Request Details
            requestDetails.setTransportCategory(determineTransportCategory(entity));
            requestDetails.setUrgencyLevel(entity.getPriority() != null ? entity.getPriority().name() : null);
            requestDetails.setServiceDate(entity.getServiceDate());
            requestDetails.setPickUpDateTime(entity.getPickupDateTime());
            requestDetails.setDropOffDateTime(entity.getDropOffDateTime());
            requestDetails.setTimingFlexible(entity.getTimingFlexible());
            requestDetails.setIsReturnTrip(entity.getIsReturnTrip());
            requestDetails.setAdditionalNotes(entity.getAdditionalNotes());

            // Map Locations
            locations.setPickUpLocation(entity.getPickUpLocation());
            locations.setDestination(entity.getDestination());
            locations.setHasMultipleStops(entity.getHasMultipleStops());
            locations.setAdditionalStops(entity.getAdditionalStops() != null ? List.of(entity.getAdditionalStops().split(",")) : null);

            // Map Contact Information (Assuming these are derived from clientId or authentication)

            User userDetails = userRepository.findById(entity.getClientId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + entity.getClientId()));

            String fullName = userDetails.getFirstName() + " " + userDetails.getLastName();
            contactInfo.setName(fullName); // Replace with actual logic
            contactInfo.setPhoneNumber(userDetails.getPhone()); // Replace with actual logic
            contactInfo.setEmailAddress(userDetails.getEmail()); // Replace with actual logic

            // Map Category-Specific Details based on boolean flags
            if (Boolean.TRUE.equals(entity.getIsPassenger())) {
                BookingRequestResponseDTO.PassengerDetails passengerDetails = new BookingRequestResponseDTO.PassengerDetails();
                passengerDetails.setEventType(entity.getEventType());
                passengerDetails.setNumberOfPassengers(entity.getNumberOfPassengers());
                passengerDetails.setLuggageNeeded(entity.getLuggageNeeded());
                passengerDetails.setLuggageDetails(entity.getLuggageDetails());
                passengerDetails.setVehiclePreferenceType(entity.getVehiclePreferenceType());

                // Special Requests
                if (Boolean.TRUE.equals(entity.getExtraAmenitiesRequired())) {
                    BookingRequestResponseDTO.SpecialRequests specialRequests = new BookingRequestResponseDTO.SpecialRequests();
//                    specialRequests.setAmenities(entity.getAmenities());
                    specialRequests.setOtherRequests(entity.getOtherRequests());
                    passengerDetails.setSpecialRequests(specialRequests);
                }

                // Waiting Time Details
                BookingRequestResponseDTO.WaitingTimeDetails waitingTimeDetails = new BookingRequestResponseDTO.WaitingTimeDetails();
                waitingTimeDetails.setWaitOnSite(entity.getWaitOnSite());
                waitingTimeDetails.setAdditionalStopsDetails(entity.getAdditionalStopsDetails());
                passengerDetails.setWaitingTimeDetails(waitingTimeDetails);

                // Budget and Pricing
                BookingRequestResponseDTO.BudgetAndPricing budgetAndPricing = new BookingRequestResponseDTO.BudgetAndPricing();
                budgetAndPricing.setBudgetRange(entity.getBudgetRange());
                passengerDetails.setBudgetAndPricing(budgetAndPricing);

                // Quote Comparison Preferences
                BookingRequestResponseDTO.QuoteComparisonPreferences quotePrefs = new BookingRequestResponseDTO.QuoteComparisonPreferences();
                quotePrefs.setPreferredFeatures(entity.getPreferredFeatures() != null ? List.of(entity.getPreferredFeatures().split(",")) : null);
                passengerDetails.setQuoteComparisonPreferences(quotePrefs);

                categoryDetails.setPassengerDetails(passengerDetails);
            }

            if (Boolean.TRUE.equals(entity.getIsCargo())) {
                BookingRequestResponseDTO.CargoDetails cargoDetails = new BookingRequestResponseDTO.CargoDetails();
                cargoDetails.setSupplyType(entity.getSupplyType());
                cargoDetails.setEstimatedWeightKg(entity.getEstimatedWeightKg());
                cargoDetails.setVolumeCubicMeters(entity.getVolumeCubicMeters());
                cargoDetails.setPackageSize(entity.getPackageSize());
                cargoDetails.setMaterialType(entity.getMaterialType());
                cargoDetails.setNeedsFragileHandling(entity.getNeedsFragileHandling());
                cargoDetails.setEstimatedValue(entity.getEstimatedValue());
                cargoDetails.setHandlingInstruction(entity.getHandlingInstruction());
                cargoDetails.setOffloadingHelpRequired(entity.getOffloadingHelpRequired());
                categoryDetails.setCargoDetails(cargoDetails);
            }

            if (Boolean.TRUE.equals(entity.getIsMedical())) {
                BookingRequestResponseDTO.MedicalDetails medicalDetails = new BookingRequestResponseDTO.MedicalDetails();
                medicalDetails.setMedicalItemType(entity.getMedicalItemType());
                medicalDetails.setRefrigerationRequired(entity.getRefrigerationRequiredMedical());
                medicalDetails.setNeedsFragileHandling(entity.getNeedsFragileHandlingMedical());
                categoryDetails.setMedicalDetails(medicalDetails);
            }

            if (Boolean.TRUE.equals(entity.getIsFurniture())) {
                BookingRequestResponseDTO.FurnitureDetails furnitureDetails = new BookingRequestResponseDTO.FurnitureDetails();
                furnitureDetails.setFurnitureType(entity.getFurnitureType());
                furnitureDetails.setRequiresDisassembly(entity.getRequiresDisassembly());
                furnitureDetails.setHandlingInstruction(entity.getHandlingInstructionFurniture());
                furnitureDetails.setOffloadingHelpRequired(entity.getOffloadingHelpRequiredFurniture());
                categoryDetails.setFurnitureDetails(furnitureDetails);
            }

            if (Boolean.TRUE.equals(entity.getIsFood())) {
                BookingRequestResponseDTO.FoodDetails foodDetails = new BookingRequestResponseDTO.FoodDetails();
                foodDetails.setFoodType(entity.getFoodType());
                foodDetails.setRequiresHotBox(entity.getRequiresHotBox());
                foodDetails.setRefrigerationRequired(entity.getRefrigerationRequiredFood());
                foodDetails.setDietaryRestriction(entity.getDietaryRestriction());
                foodDetails.setEstimatedWeightKg(entity.getEstimatedWeightKgFood());
                foodDetails.setPackageSize(entity.getPackageSizeFood());
                categoryDetails.setFoodDetails(foodDetails);
            }

            if (Boolean.TRUE.equals(entity.getIsEquipment())) {
                BookingRequestResponseDTO.EquipmentDetails equipmentDetails = new BookingRequestResponseDTO.EquipmentDetails();
                equipmentDetails.setEquipmentList(entity.getEquipmentList());
                equipmentDetails.setSetupRequired(entity.getSetupRequired());
                categoryDetails.setEquipmentDetails(equipmentDetails);
            }

            // Set nested structures
            bookingDetails.setRequestDetails(requestDetails);
            bookingDetails.setLocations(locations);
            bookingDetails.setContactInformation(contactInfo);
            bookingDetails.setCategorySpecificDetails(categoryDetails);
            dto.setBookingRequest(bookingDetails);

            return dto;
        }

    private String determineTransportCategory(BookingRequest entity) {
            if (Boolean.TRUE.equals(entity.getIsPassenger())) return "Passenger";
            if (Boolean.TRUE.equals(entity.getIsCargo())) return "Cargo";
            if (Boolean.TRUE.equals(entity.getIsMedical())) return "Medical";
            if (Boolean.TRUE.equals(entity.getIsFurniture())) return "Furniture";
            if (Boolean.TRUE.equals(entity.getIsFood())) return "Food";
            if (Boolean.TRUE.equals(entity.getIsEquipment())) return "Equipment";
            return null;
        }

    public Page<BookingRequestResponseDTO> mapPageToPageResponse(Page<BookingRequest> bookingPage) {
        return bookingPage.map(this::mapToResponseDTO);
    }


    public List<DriverResponse> getAvailableDrivers() {

        // Get authenticated user
        SecurityUserDetails userDetails = securityUserDetailsService.getSecurityUserDetails();

        // Fetch drivers
        List<User> drivers;
        if (userDetails.getRole() == EnumRoles.PROVIDER) {
            // For PROVIDER, only get drivers with matching providerId
            drivers = userRepository.findByRolesAndProviderId(EnumRoles.DRIVER, String.valueOf(userDetails.getId()));
        } else {
            // For ADMIN or CUSTOMER_SERVICE, get all DRIVERs
            drivers = userRepository.findByRoles(EnumRoles.DRIVER);
        }

        // Map to DriverResponse
        return drivers.stream()
                .map(user -> {
                    DriverResponse response = new DriverResponse();
                    response.setId(user.getId());
                    response.setFullName(buildFullName(user));
                    response.setAddress(user.getAddress());
                    response.setEmail(user.getEmail());
                    return response;
                })
                .collect(Collectors.toList());
    }

    private String buildFullName(User user) {
        StringBuilder fullName = new StringBuilder(user.getFirstName());
        if (user.getMiddleName() != null && !user.getMiddleName().isEmpty()) {
            fullName.append(" ").append(user.getMiddleName());
        }
        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
            fullName.append(" ").append(user.getLastName());
        }
        return fullName.toString();
    }



    @Override
    public Page<BookingRequest> getAllBookings(BookingStatus status,
            String pickUpLocation, String destination, String additionalStops,
            LocalDateTime serviceStartDate, LocalDateTime serviceEndDate,
            LocalDateTime pickupStartDateTime, LocalDateTime pickupEndDateTime,
            LocalDateTime createdStartDate, LocalDateTime createdEndDate,
            EnumPriority priority, Boolean isPassenger, Integer numberOfPassengers,
            String eventType, Boolean isCargo, Double estimatedWeightKg, String supplyType,
            Boolean isMedical, String medicalItemType, Boolean isFurniture, String furnitureType,
            Boolean isFood, String foodType, Boolean isEquipment, String equipmentItem, String city, String state, String country,
            Pageable pageable) {

        SecurityUserDetails userDetails = securityUserDetailsService.getSecurityUserDetails();
        // Check if the user is a provider
        if (userDetails.getRole() == EnumRoles.PROVIDER) {
            country = userDetails.getCountry();
        }

        return bookingRepository.findByStatusAndFilters(
                status, pickUpLocation, destination, additionalStops,
                serviceStartDate, serviceEndDate, pickupStartDateTime, pickupEndDateTime,
                createdStartDate, createdEndDate, priority,
                isPassenger, numberOfPassengers, eventType,
                isCargo, estimatedWeightKg, supplyType,
                isMedical, medicalItemType,
                isFurniture, furnitureType,
                isFood, foodType,
                isEquipment, equipmentItem, city, state, country,
                pageable);
    }

}