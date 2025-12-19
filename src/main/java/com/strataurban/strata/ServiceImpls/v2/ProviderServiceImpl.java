package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.DTOs.v2.ProviderDashboard;
import com.strataurban.strata.DTOs.v2.RatingRequest;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ProviderDocument;
import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import com.strataurban.strata.Enums.BookingStatus;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.BookingRepository;
import com.strataurban.strata.Repositories.v2.ProviderDocumentRepository;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Repositories.v2.TransportRepository;
import com.strataurban.strata.Services.v2.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderDocumentRepository providerDocumentRepository;
    private final BookingRepository bookingRepository;
    private final TransportRepository transportRepository;

    @Autowired
    public ProviderServiceImpl(
            ProviderRepository providerRepository,
            ProviderDocumentRepository providerDocumentRepository,
            BookingRepository bookingRepository,
            TransportRepository transportRepository) {
        this.providerRepository = providerRepository;
        this.providerDocumentRepository = providerDocumentRepository;
        this.bookingRepository = bookingRepository;
        this.transportRepository = transportRepository;
    }

    @Override
    public Provider registerProvider(Provider provider) {
        provider.setRoles(EnumRoles.PROVIDER); // Ensure the role is set to PROVIDER
        return providerRepository.save(provider);
    }

    @Override
    public Provider getProviderById(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + id));
    }

    @Override
    public Provider updateProvider(Long id, Provider provider) {
        Provider existingProvider = getProviderById(id);
        // Update fields (only those that are allowed to be updated)
        existingProvider.setCompanyName(provider.getCompanyName());
        existingProvider.setDescription(provider.getDescription());
        existingProvider.setAddress(provider.getAddress());
        existingProvider.setCity(provider.getCity());
        existingProvider.setState(provider.getState());
        existingProvider.setZipCode(provider.getZipCode());
        existingProvider.setCountry(provider.getCountry());
//        existingProvider.setPhoneNumber(provider.getPhoneNumber());
        existingProvider.setEmail(provider.getEmail());
        existingProvider.setPreferredLanguage(provider.getPreferredLanguage());
        existingProvider.setCompanyLogoUrl(provider.getCompanyLogoUrl());
        existingProvider.setCompanyBannerUrl(provider.getCompanyBannerUrl());
        existingProvider.setPrimaryContactPosition(provider.getPrimaryContactPosition());
        existingProvider.setPrimaryContactDepartment(provider.getPrimaryContactDepartment());
        existingProvider.setServiceTypes(provider.getServiceTypes());
        existingProvider.setCompanyAddress(provider.getCompanyAddress());
        existingProvider.setCompanyRegistrationNumber(provider.getCompanyRegistrationNumber());
        existingProvider.setCompanyBusinessEmail(provider.getCompanyBusinessEmail());
        existingProvider.setCompanyBusinessPhone(provider.getCompanyBusinessPhone());
        existingProvider.setCompanyBusinessWebsite(provider.getCompanyBusinessWebsite());
        existingProvider.setCompanyBusinessType(provider.getCompanyBusinessType());
        return providerRepository.save(existingProvider);
    }

    @Override
    public ProviderDocument uploadDocuments(Long providerId, ProviderDocument documents) {
        Provider provider = getProviderById(providerId);
        // Assuming ProviderDocument has a providerId field (we'll add it below)
        documents.setProviderId(providerId);
        return providerDocumentRepository.save(documents);
    }

    @Override
    public List<ProviderDocument> getProviderDocuments(Long providerId) {
        return providerDocumentRepository.findAllByProviderId(providerId);
    }

    @Override
    public ProviderDocument updateProviderDocuments(Long providerId, ProviderDocument documents) {
        ProviderDocument existingDocuments = getProviderDocuments(providerId).get(0);
        existingDocuments.setProviderRegistrationDocument(documents.getProviderRegistrationDocument());
        existingDocuments.setProviderLicenseDocument(documents.getProviderLicenseDocument());
        existingDocuments.setProviderNameDocument(documents.getProviderNameDocument());
        existingDocuments.setTaxDocument(documents.getTaxDocument());
        return providerDocumentRepository.save(existingDocuments);
    }

    @Override
    public void deleteProvider(Long id) {
        Provider provider = getProviderById(id);
        providerRepository.delete(provider);
    }

    @Override
    public Page<Provider> getAllProviders(Pageable pageable) {
        return providerRepository.findAll(pageable);
    }

    @Override
    public Page<Provider> searchProviders(String name, String serviceType, String city, Double minRating, Pageable pageable) {
        return providerRepository.findByCriteria(name, serviceType, city, minRating, pageable);
    }

    @Override
    public void rateProvider(Long providerId, RatingRequest rating) {
        Provider provider = getProviderById(providerId);
        int currentNumberOfRatings = provider.getNumberOfRatings();
        double currentRating = provider.getRating() != null ? provider.getRating() : 0.0;

        // Calculate new average rating
        double newTotalRating = (currentRating * currentNumberOfRatings) + rating.getRating();
        int newNumberOfRatings = currentNumberOfRatings + 1;
        double newAverageRating = newTotalRating / newNumberOfRatings;

        provider.setRating(newAverageRating);
        provider.setNumberOfRatings(newNumberOfRatings);
        providerRepository.save(provider);
    }

    @Override
    public ProviderDashboard getProviderDashboard(Long providerId) {
        // Today's bookings
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        List<BookingRequest> todaysBookings = bookingRepository.findByProviderId(providerId).stream()
                .filter(booking -> booking.getServiceDate() != null &&
                        booking.getServiceDate().isAfter(startOfDay) &&
                        booking.getServiceDate().isBefore(endOfDay))
                .toList();
        int todaysBookingsCount = todaysBookings.size();

        // Pending confirmations
        int pendingConfirmations = (int) bookingRepository.findByProviderIdAndStatus(providerId, BookingStatus.PENDING)
                .stream().count();

        // Vehicles assigned
        long vehiclesAssigned = transportRepository.countByProviderId(providerId);

        // Monthly revenue (simplified calculation)
        LocalDateTime startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
        double monthlyRevenue = bookingRepository.findByProviderId(providerId).stream()
                .filter(booking -> booking.getServiceDate() != null &&
                        booking.getServiceDate().isAfter(startOfMonth) &&
                        booking.getServiceDate().isBefore(endOfMonth) &&
                        booking.getStatus() == BookingStatus.COMPLETED)
                .mapToDouble(booking -> {
                    try {
                        return Double.parseDouble(booking.getRateInformation().replace("$", ""));
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();

        return new ProviderDashboard(todaysBookingsCount, pendingConfirmations, (int) vehiclesAssigned, monthlyRevenue);
    }
}