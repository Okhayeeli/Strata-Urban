package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.DTOs.v2.ProviderDTO;
import com.strataurban.strata.DTOs.v2.ProviderDashboard;
import com.strataurban.strata.DTOs.v2.ProviderDocumentDTO;
import com.strataurban.strata.DTOs.v2.RatingRequest;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ProviderDocument;
import com.strataurban.strata.Services.v2.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/providers")
@Tag(name = "Provider Management", description = "APIs for managing providers")
public class ProviderRestController {

    private final ProviderService providerService;

    @Autowired
    public ProviderRestController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new provider", description = "Registers a new provider in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid provider data"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER or ADMIN can register a provider. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<ProviderDTO> registerProvider(@RequestBody Provider provider) {
        try {
            Provider savedProvider = providerService.registerProvider(provider);
            return ResponseEntity.ok(mapToDTO(savedProvider));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER or ADMIN can register a provider. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get provider profile by ID", description = "Fetches the profile of a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #id), DRIVER, ADMIN, or DEVELOPER can access this endpoint. CLIENT and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #id) or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<ProviderDTO> getProviderById(@PathVariable Long id) {
        try {
            Provider provider = providerService.getProviderById(id);
            return ResponseEntity.ok(mapToDTO(provider));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #id), DRIVER, ADMIN, or DEVELOPER can access this endpoint. CLIENT and others are restricted.");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update provider profile", description = "Updates the profile of a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider updated successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #id) or ADMIN can update a provider. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #id) or hasRole('ADMIN')")
    public ResponseEntity<ProviderDTO> updateProvider(
            @PathVariable Long id,
            @RequestBody Provider provider) {
        try {
            Provider updatedProvider = providerService.updateProvider(id, provider);
            return ResponseEntity.ok(mapToDTO(updatedProvider));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #id) or ADMIN can update a provider. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @PostMapping("/{id}/documents")
    @Operation(summary = "Upload provider documents", description = "Uploads documents for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents uploaded successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #id) or ADMIN can upload documents. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #id) or hasRole('ADMIN')")
    public ResponseEntity<ProviderDocumentDTO> uploadDocuments(
            @PathVariable Long id,
            @RequestBody ProviderDocument documents) {
        try {
            ProviderDocument savedDocuments = providerService.uploadDocuments(id, documents);
            return ResponseEntity.ok(mapToDocumentDTO(savedDocuments));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #id) or ADMIN can upload documents. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping("/{id}/documents")
    @Operation(summary = "Get provider documents", description = "Fetches documents for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Documents not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #id), DRIVER, ADMIN, or DEVELOPER can access documents. CLIENT and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #id) or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<ProviderDocumentDTO> getProviderDocuments(@PathVariable Long id) {
        try {
            ProviderDocument documents = providerService.getProviderDocuments(id);
            return ResponseEntity.ok(mapToDocumentDTO(documents));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #id), DRIVER, ADMIN, or DEVELOPER can access documents. CLIENT and others are restricted.");
        }
    }

    @PutMapping("/{id}/documents")
    @Operation(summary = "Update provider documents", description = "Updates documents for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents updated successfully"),
            @ApiResponse(responseCode = "404", description = "Documents not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #id) or ADMIN can update documents. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #id) or hasRole('ADMIN')")
    public ResponseEntity<ProviderDocumentDTO> updateProviderDocuments(
            @PathVariable Long id,
            @RequestBody ProviderDocument documents) {
        try {
            ProviderDocument updatedDocuments = providerService.updateProviderDocuments(id, documents);
            return ResponseEntity.ok(mapToDocumentDTO(updatedDocuments));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #id) or ADMIN can update documents. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete provider account", description = "Deletes a specific provider account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN can delete a provider. PROVIDER, CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        try {
            providerService.deleteProvider(id);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN can delete a provider. PROVIDER, CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping
    @Operation(summary = "Get all providers", description = "Fetches all providers in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN or DEVELOPER can access all providers. PROVIDER, CLIENT, DRIVER, and others are restricted.")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<ProviderDTO>> getAllProviders() {
        try {
            List<Provider> providers = providerService.getAllProviders();
            List<ProviderDTO> providerDTOs = providers.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(providerDTOs);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only ADMIN or DEVELOPER can access all providers. PROVIDER, CLIENT, DRIVER, and others are restricted.");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search providers", description = "Searches providers by service type, city, and minimum rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT, PROVIDER, DRIVER, ADMIN, or DEVELOPER can search providers. Others are restricted.")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ResponseEntity<List<ProviderDTO>> searchProviders(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minRating) {
        try {
            List<Provider> providers = providerService.searchProviders(serviceType, city, minRating);
            List<ProviderDTO> providerDTOs = providers.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(providerDTOs);
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT, PROVIDER, DRIVER, ADMIN, or DEVELOPER can search providers. Others are restricted.");
        }
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate a provider", description = "Allows a client to rate a provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider rated successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only CLIENT can rate a provider. PROVIDER, DRIVER, ADMIN, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> rateProvider(
            @PathVariable Long id,
            @RequestBody RatingRequest rating) {
        try {
            providerService.rateProvider(id, rating);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only CLIENT can rate a provider. PROVIDER, DRIVER, ADMIN, DEVELOPER, and others are restricted.");
        }
    }

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Get provider dashboard", description = "Fetches dashboard statistics for a specific provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Provider not found"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only PROVIDER (if principal.id == #id) or ADMIN can access the dashboard. CLIENT, DRIVER, DEVELOPER, and others are restricted.")
    })
    @PreAuthorize("(hasRole('PROVIDER') and principal.id == #id) or hasRole('ADMIN')")
    public ResponseEntity<ProviderDashboard> getProviderDashboard(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(providerService.getProviderDashboard(id));
        } catch (SecurityException e) {
            throw new AccessDeniedException("Access denied: Only PROVIDER (if principal.id == #id) or ADMIN can access the dashboard. CLIENT, DRIVER, DEVELOPER, and others are restricted.");
        }
    }

    // Helper method to map Provider entity to ProviderDTO
    private ProviderDTO mapToDTO(Provider provider) {
        return new ProviderDTO(
                provider.getId(),
                provider.getTitle(),
                provider.getFirstName(),
                provider.getMiddleName(),
                provider.getLastName(),
                provider.isEmailVerified(),
                provider.getEmail(),
                provider.getUsername(),
                provider.getPhone(),
                provider.getPhone2(),
                provider.getAddress(),
                provider.getPreferredLanguage(),
                provider.getCity(),
                provider.getState(),
                provider.getCountry(),
                provider.getRoles(),
                provider.getImageUrl(),
                provider.getCompanyLogoUrl(),
                provider.getPrimaryContactPosition(),
                provider.getPrimaryContactDepartment(),
                provider.getCompanyBannerUrl(),
                provider.getSupplierCode(),
                provider.getCompanyName(),
                provider.getCompanyAddress(),
                provider.getCompanyRegistrationNumber(),
                provider.getDescription(),
                provider.getZipCode(),
                provider.getRating(),
                provider.getNumberOfRatings(),
                provider.getServiceTypes(),
                provider.getServiceAreas()
        );
    }

    // Helper method to map ProviderDocument entity to ProviderDocumentDTO
    private ProviderDocumentDTO mapToDocumentDTO(ProviderDocument document) {
        return new ProviderDocumentDTO(
                document.getId(),
                document.getProviderRegistrationDocument(),
                document.getProviderLicenseDocument(),
                document.getProviderNameDocument(),
                document.getTaxDocument()
        );
    }
}