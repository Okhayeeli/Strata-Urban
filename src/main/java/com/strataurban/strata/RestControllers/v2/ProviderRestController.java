package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ProviderDocument;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Security.LoggedUser;
import com.strataurban.strata.Security.SecurityUserDetails;
import com.strataurban.strata.Services.v2.BookingService;
import com.strataurban.strata.Services.v2.ProviderService;
import com.strataurban.strata.Services.v2.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/providers")
@Tag(name = "Provider Management", description = "APIs for managing providers")
public class ProviderRestController {

    private final ProviderService providerService;
    private final UserService userService;
    private final BookingService bookingService;

    @Autowired
    public ProviderRestController(ProviderService providerService, UserService userService, BookingService bookingService) {
        this.providerService = providerService;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new provider", description = "Registers a new provider in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid provider data"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER or ADMIN can register a provider. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderDTO> registerProvider(@RequestBody Provider provider) {
        Provider savedProvider = providerService.registerProvider(provider);
        return ResponseEntity.ok(mapToDTO(savedProvider));
    }

    @GetMapping(value = {"", "/{id}"})
    @Operation(summary = "Get provider profile", description = "Fetches a provider's profile. For PROVIDER role, ID is optional and defaults to the authenticated user's ID. For ADMIN, CUSTOMER_SERVICE, DRIVER, or DEVELOPER, ID is required in the path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "ID required for ADMIN, CUSTOMER_SERVICE, DRIVER, or DEVELOPER roles"),
            @ApiResponse(responseCode = "403", description = "Access denied: PROVIDER can only access their own profile"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'DRIVER', 'ADMIN', 'CUSTOMER_SERVICE', 'DEVELOPER')")
    public ResponseEntity<ProviderDTO> getProviderById(@PathVariable(required = false) Long id, @LoggedUser SecurityUserDetails userDetails) {
        Long providerId = resolveProviderId(id, userDetails, List.of("ADMIN", "CUSTOMER_SERVICE", "DRIVER", "DEVELOPER"));
        Provider provider = providerService.getProviderById(providerId);
        return ResponseEntity.ok(mapToDTO(provider));
    }

    @PutMapping(value = {"", "/{id}"})
    @Operation(summary = "Update provider profile", description = "Updates a provider's profile. For PROVIDER role, ID is optional and defaults to the authenticated user's ID. For ADMIN, ID is required in the path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider updated successfully"),
            @ApiResponse(responseCode = "400", description = "ID required for ADMIN role"),
            @ApiResponse(responseCode = "403", description = "Access denied: PROVIDER can only update their own profile"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderDTO> updateProvider(@PathVariable(required = false) Long id, @RequestBody Provider provider, @LoggedUser SecurityUserDetails userDetails) {
        Long providerId = resolveProviderId(id, userDetails, List.of("ADMIN"));
        Provider updatedProvider = providerService.updateProvider(providerId, provider);
        return ResponseEntity.ok(mapToDTO(updatedProvider));
    }

    @PostMapping(value = {"", "/{id}/documents"})
    @Operation(summary = "Upload provider documents", description = "Uploads documents for a provider. For PROVIDER role, ID is optional and defaults to the authenticated user's ID. For ADMIN, ID is required in the path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "ID required for ADMIN role"),
            @ApiResponse(responseCode = "403", description = "Access denied: PROVIDER can only upload their own documents"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderDocumentDTO> uploadDocuments(@PathVariable(required = false) Long id, @RequestBody ProviderDocument documents, @LoggedUser SecurityUserDetails userDetails) {
        Long providerId = resolveProviderId(id, userDetails, List.of("ADMIN"));
        ProviderDocument savedDocuments = providerService.uploadDocuments(providerId, documents);
        return ResponseEntity.ok(mapToDocumentDTO(savedDocuments));
    }

    @GetMapping(value = {"", "/{id}/documents"})
    @Operation(summary = "Get provider documents", description = "Fetches documents for a provider. For PROVIDER role, ID is optional and defaults to the authenticated user's ID. For ADMIN, DRIVER, or DEVELOPER, ID is required in the path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "ID required for ADMIN, DRIVER, or DEVELOPER roles"),
            @ApiResponse(responseCode = "403", description = "Access denied: PROVIDER can only access their own documents"),
            @ApiResponse(responseCode = "404", description = "Documents not found")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'DRIVER', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<ProviderDocumentDTO> getProviderDocuments(@PathVariable(required = false) Long id, @LoggedUser SecurityUserDetails userDetails) {
        Long providerId = resolveProviderId(id, userDetails, List.of("ADMIN", "DRIVER", "DEVELOPER"));
        ProviderDocument documents = providerService.getProviderDocuments(providerId);
        return ResponseEntity.ok(mapToDocumentDTO(documents));
    }

    @PutMapping(value = {"", "/{id}/documents"})
    @Operation(summary = "Update provider documents", description = "Updates documents for a provider. For PROVIDER role, ID is optional and defaults to the authenticated user's ID. For ADMIN, ID is required in the path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents updated successfully"),
            @ApiResponse(responseCode = "400", description = "ID required for ADMIN role"),
            @ApiResponse(responseCode = "403", description = "Access denied: PROVIDER can only update their own documents"),
            @ApiResponse(responseCode = "404", description = "Documents not found")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderDocumentDTO> updateProviderDocuments(@PathVariable(required = false) Long id, @RequestBody ProviderDocument documents, @LoggedUser SecurityUserDetails userDetails) {
        Long providerId = resolveProviderId(id, userDetails, List.of("ADMIN"));
        ProviderDocument updatedDocuments = providerService.updateProviderDocuments(providerId, documents);
        return ResponseEntity.ok(mapToDocumentDTO(updatedDocuments));
    }

    @DeleteMapping
    @Operation(summary = "Delete provider account", description = "Deletes a provider account. For PROVIDER role, ID is taken from the request body and must match the authenticated user's ID. For ADMIN, any provider ID can be specified in the request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Provider ID is required"),
            @ApiResponse(responseCode = "403", description = "Access denied: PROVIDER can only delete their own account"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<Void> deleteProvider(@RequestBody RequestBodyIdDto request, @LoggedUser SecurityUserDetails userDetails) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Provider ID is required");
        }
        if (userDetails.getRole().name().equals("PROVIDER") && !request.getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("PROVIDER can only delete their own account");
        }
        providerService.deleteProvider(request.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    @Operation(summary = "Get all providers", description = "Fetches a paginated list of all providers in the system, accessible to all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
//    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'DRIVER', 'ADMIN', 'CUSTOMER_SERVICE', 'DEVELOPER')")
    public ResponseEntity<Page<ProviderDTO>> getAllProviders(Pageable pageable) {
        Page<Provider> providers = providerService.getAllProviders(pageable);
        Page<ProviderDTO> providerDTOs = providers.map(this::mapToDTO);
        return ResponseEntity.ok(providerDTOs);
    }

    @GetMapping("/search")
    @Operation(summary = "Search providers", description = "Searches providers by name (contains, case-insensitive), service type, city, and minimum rating, returning a paginated list, accessible to all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query or pagination parameters")
    })
    public ResponseEntity<Page<ProviderDTO>> searchProviders(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minRating,
            Pageable pageable) {
        Page<Provider> providers = providerService.searchProviders(name, serviceType, city, minRating, pageable);
        Page<ProviderDTO> providerDTOs = providers.map(this::mapToDTO);
        return ResponseEntity.ok(providerDTOs);
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate a provider", description = "Allows a client to rate a provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider rated successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT can rate a provider")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> rateProvider(@PathVariable Long id, @RequestBody RatingRequest rating) {
        providerService.rateProvider(id, rating);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Get provider dashboard", description = "Fetches dashboard statistics for a provider. For PROVIDER role, ID must match the authenticated user's ID. For ADMIN, any provider ID is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: PROVIDER can only access their own dashboard")
    })
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderDashboard> getProviderDashboard(@PathVariable Long id, @LoggedUser SecurityUserDetails userDetails) {
        if (userDetails.getRole().name().equals("PROVIDER") && !id.equals(userDetails.getId())) {
            throw new AccessDeniedException("PROVIDER can only access their own dashboard");
        }
        return ResponseEntity.ok(providerService.getProviderDashboard(id));
    }

    @PostMapping("/signup/driver")
    @Operation(summary = "Register a driver for a Provider", description = "Creates a new driver (ADMIN, CUSTOMER_SERVICE, DEVELOPER, Provider)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration request")
    })
    @PreAuthorize("hasAnyRole('CUSTOMER_SERVICE', 'ADMIN', 'PROVIDER', 'DRIVER', 'DEVELOPER')")
    public ResponseEntity<User> registerDriver(@Valid @RequestBody AdminRegistrationRequest request, @LoggedUser SecurityUserDetails userDetails) {
        return ResponseEntity.ok(userService.registerDriver(request, userDetails));
    }

    @GetMapping("/available-drivers")
    @Operation(summary = "Get available drivers for a booking", description = "Returns a list of drivers (id, full name, address, email) who can be assigned to a CONFIRMED or CLAIMED booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of available drivers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Booking is not in CONFIRMED or CLAIMED status"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER, ADMIN, or CUSTOMER_SERVICE can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        return ResponseEntity.ok(bookingService.getAvailableDrivers());
    }

    private Long resolveProviderId(Long id, SecurityUserDetails userDetails, List<String> rolesRequiringId) {
        if (id == null && rolesRequiringId.contains(userDetails.getRole().name())) {
            throw new IllegalArgumentException("ID is required for " + String.join(", ", rolesRequiringId) + " roles");
        }
        Long providerId = userDetails.getRole().name().equals("PROVIDER") ? userDetails.getId() : id;
        if (userDetails.getRole().name().equals("PROVIDER") && id != null && !id.equals(userDetails.getId())) {
            throw new AccessDeniedException("PROVIDER can only access their own data");
        }
        return providerId;
    }

    private ProviderDTO mapToDTO(Provider provider) {
        return new ProviderDTO(provider.getId(), provider.getTitle(), provider.getFirstName(), provider.getMiddleName(),
                provider.getLastName(), provider.isEmailVerified(), provider.getEmail(), provider.getUsername(),
                provider.getPhone(), provider.getPhone2(), provider.getAddress(), provider.getPreferredLanguage(),
                provider.getCity(), provider.getState(), provider.getCountry(), provider.getRoles(), provider.getImageUrl(),
                provider.getCompanyLogoUrl(), provider.getPrimaryContactPosition(), provider.getPrimaryContactDepartment(),
                provider.getCompanyBannerUrl(), provider.getSupplierCode(), provider.getCompanyName(),
                provider.getCompanyAddress(), provider.getCompanyRegistrationNumber(), provider.getDescription(),
                provider.getZipCode(), provider.getRating(), provider.getNumberOfRatings(), provider.getServiceTypes(),
                provider.getServiceAreas());
    }

    private ProviderDocumentDTO mapToDocumentDTO(ProviderDocument document) {
        return new ProviderDocumentDTO(document.getId(), document.getProviderRegistrationDocument(),
                document.getProviderLicenseDocument(), document.getProviderNameDocument(), document.getTaxDocument());
    }
}