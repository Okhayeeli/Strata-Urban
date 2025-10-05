package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface BookingRepository extends JpaRepository<BookingRequest, Long>, JpaSpecificationExecutor<BookingRequest> {

    // Find all bookings for a specific client
    Page<BookingRequest> findByClientId(Long clientId, Pageable pageable);
    List<BookingRequest> findByClientId(Long clientId);

    // Find all bookings for a specific provider
    List<BookingRequest> findByProviderId(Long providerId);
    Page<BookingRequest> findByProviderId(Long providerId, Pageable pageable);

    // Find bookings by provider and status
    Page<BookingRequest> findByProviderIdAndStatus(Long providerId, BookingStatus status, Pageable pageable);
    List<BookingRequest> findByProviderIdAndStatus(Long providerId, BookingStatus status);

    // Find booking history for a client (e.g., completed or cancelled bookings)
    Page<BookingRequest> findByClientIdAndStatusIn(Long clientId, List<BookingStatus> statuses, Pageable pageable);

    // Find booking history for a provider (e.g., completed or cancelled bookings)
    List<BookingRequest> findByProviderIdAndStatusIn(Long providerId, List<BookingStatus> statuses);

    // New query: Find bookings by transport category (e.g., Passenger, Cargo)
    @Query("SELECT br FROM BookingRequest br WHERE "
            + "(:isPassenger IS NULL OR br.isPassenger = :isPassenger) "
            + "AND (:isCargo IS NULL OR br.isCargo = :isCargo) "
            + "AND (:isMedical IS NULL OR br.isMedical = :isMedical) "
            + "AND (:isFurniture IS NULL OR br.isFurniture = :isFurniture) "
            + "AND (:isFood IS NULL OR br.isFood = :isFood) "
            + "AND (:isEquipment IS NULL OR br.isEquipment = :isEquipment)")
    List<BookingRequest> findByTransportCategory(
            @Param("isPassenger") Boolean isPassenger,
            @Param("isCargo") Boolean isCargo,
            @Param("isMedical") Boolean isMedical,
            @Param("isFurniture") Boolean isFurniture,
            @Param("isFood") Boolean isFood,
            @Param("isEquipment") Boolean isEquipment);

//    // New query: Find passenger bookings with specific amenities
//    @Query("SELECT br FROM BookingRequest br WHERE br.isPassenger = true "
//            + "AND (:amenity IS NULL OR :amenity MEMBER OF br.amenities)")
//    Page<BookingRequest> findPassengerBookingsByAmenity(
//            @Param("amenity") String amenity, Pageable pageable);

    // New query: Find cargo bookings by weight range
    @Query("SELECT br FROM BookingRequest br WHERE br.isCargo = true "
            + "AND (:minWeight IS NULL OR br.estimatedWeightKg >= :minWeight) "
            + "AND (:maxWeight IS NULL OR br.estimatedWeightKg <= :maxWeight)")
    Page<BookingRequest> findCargoBookingsByWeightRange(
            @Param("minWeight") Double minWeight,
            @Param("maxWeight") Double maxWeight, Pageable pageable);

    // New query: Find medical bookings requiring refrigeration
    List<BookingRequest> findByIsMedicalTrueAndRefrigerationRequiredMedicalTrue();

    // New query: Find food bookings by food type
    List<BookingRequest> findByIsFoodTrueAndFoodType(@Param("foodType") String foodType);

    // New query: Find equipment bookings requiring setup
    List<BookingRequest> findByIsEquipmentTrueAndSetupRequiredTrue();


    @Query("SELECT br FROM BookingRequest br WHERE (:status IS NULL OR br.status = :status) "
                + "AND (:pickUpLocation IS NULL OR br.pickUpLocation LIKE %:pickUpLocation%) "
                + "AND (:destination IS NULL OR br.destination LIKE %:destination%) "
                + "AND (:additionalStops IS NULL OR br.additionalStops LIKE %:additionalStops%) "
                + "AND (:serviceStartDate IS NULL OR br.serviceDate >= :serviceStartDate) "
                + "AND (:serviceEndDate IS NULL OR br.serviceDate <= :serviceEndDate) "
                + "AND (:pickupStartDateTime IS NULL OR br.pickupDateTime >= :pickupStartDateTime) "
                + "AND (:pickupEndDateTime IS NULL OR br.pickupDateTime <= :pickupEndDateTime) "
                + "AND (:createdStartDate IS NULL OR br.createdDate >= :createdStartDate) "
                + "AND (:createdEndDate IS NULL OR br.createdDate <= :createdEndDate) "
                + "AND (:priority IS NULL OR br.priority = :priority) "
                + "AND (:isPassenger IS NULL OR br.isPassenger = :isPassenger) "
                + "AND (:numberOfPassengers IS NULL OR br.numberOfPassengers = :numberOfPassengers) "
                + "AND (:eventType IS NULL OR br.eventType = :eventType) "
                + "AND (:isCargo IS NULL OR br.isCargo = :isCargo) "
                + "AND (:estimatedWeightKg IS NULL OR br.estimatedWeightKg = :estimatedWeightKg) "
                + "AND (:supplyType IS NULL OR br.supplyType = :supplyType) "
                + "AND (:isMedical IS NULL OR br.isMedical = :isMedical) "
                + "AND (:medicalItemType IS NULL OR br.medicalItemType = :medicalItemType) "
                + "AND (:isFurniture IS NULL OR br.isFurniture = :isFurniture) "
                + "AND (:furnitureType IS NULL OR br.furnitureType = :furnitureType) "
                + "AND (:isFood IS NULL OR br.isFood = :isFood) "
                + "AND (:foodType IS NULL OR br.foodType = :foodType) "
                + "AND (:isEquipment IS NULL OR br.isEquipment = :isEquipment) "
                + "AND (:equipmentItem IS NULL OR :equipmentItem MEMBER OF br.equipmentList) "
                + "AND (:city IS NULL OR br.city LIKE %:city%) "
                + "AND (:state IS NULL OR br.state LIKE %:state%) "
                + "AND (:country IS NULL OR br.country LIKE %:country%)")
        Page<BookingRequest> findByStatusAndFilters(
                @Param("status") BookingStatus status,
                @Param("pickUpLocation") String pickUpLocation,
                @Param("destination") String destination,
                @Param("additionalStops") String additionalStops,
                @Param("serviceStartDate") LocalDateTime serviceStartDate,
                @Param("serviceEndDate") LocalDateTime serviceEndDate,
                @Param("pickupStartDateTime") LocalDateTime pickupStartDateTime,
                @Param("pickupEndDateTime") LocalDateTime pickupEndDateTime,
                @Param("createdStartDate") LocalDateTime createdStartDate,
                @Param("createdEndDate") LocalDateTime createdEndDate,
                @Param("priority") EnumPriority priority,
                @Param("isPassenger") Boolean isPassenger,
                @Param("numberOfPassengers") Integer numberOfPassengers,
                @Param("eventType") String eventType,
                @Param("isCargo") Boolean isCargo,
                @Param("estimatedWeightKg") Double estimatedWeightKg,
                @Param("supplyType") String supplyType,
                @Param("isMedical") Boolean isMedical,
                @Param("medicalItemType") String medicalItemType,
                @Param("isFurniture") Boolean isFurniture,
                @Param("furnitureType") String furnitureType,
                @Param("isFood") Boolean isFood,
                @Param("foodType") String foodType,
                @Param("isEquipment") Boolean isEquipment,
                @Param("equipmentItem") String equipmentItem,
                @Param("city") String city,
                @Param("state") String state,
                @Param("country") String country,
                Pageable pageable);

        boolean existsByIdAndClientId(Long bookingId, Long clientId);

        boolean existsByIdAndProviderId(Long entityId, Long providerId);

}