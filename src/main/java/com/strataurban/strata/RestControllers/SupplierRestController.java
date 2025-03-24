//package com.strataurban.strata.RestControllers;
//
//import com.strataurban.strata.DTOs.OfferRequestDTO;
//import com.strataurban.strata.DTOs.RequestBodyID;
//import com.strataurban.strata.DTOs.SupplierIDDTO;
//import com.strataurban.strata.Entities.Passengers.Booking;
//import com.strataurban.strata.Entities.Providers.Offer;
//import com.strataurban.strata.Entities.Providers.Provider;
//import com.strataurban.strata.Services.SupplierService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//
//@RestController
//@RequestMapping("/api/v1/supplier")
//@Tag(name = "Supplier", description = "Operations for suppliers to manage requests and offers")
//public class SupplierRestController {
//
//    @Autowired
//    SupplierService supplierService;
//
//    @Operation(
//            summary = "Register a new supplier",
//            description = "Registers a new supplier. This endpoint does not require authentication."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Supplier registered successfully",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Provider.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid supplier data", content = @Content)
//    })
//    @PostMapping("/register")
//    public Provider registerSupplier(
//            @Parameter(description = "Supplier details for registration", required = true)
//            @RequestBody Provider provider) {
//        return supplierService.registerSupplier(provider);
//    }
//
//    @Operation(
//            summary = "Update supplier details",
//            description = "Updates the details of an existing supplier. Requires JWT authentication.",
//            security = @SecurityRequirement(name = "JWT")
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Supplier updated successfully",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Provider.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid supplier data", content = @Content),
//            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
//    })
//    @PutMapping("/update")
//    public Provider updateSupplier(
//            @Parameter(description = "Updated supplier details", required = true)
//            @RequestBody Provider provider) {
//        return supplierService.updateSupplier(provider);
//    }
//
//    @Operation(
//            summary = "View all requests for a supplier",
//            description = "Retrieves a list of all booking requests for a supplier. Requires JWT authentication.",
//            security = @SecurityRequirement(name = "JWT")
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "List of booking requests retrieved successfully",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid supplier ID", content = @Content),
//            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
//    })
//    @GetMapping("/all-requests")
//    public List<Booking> viewRequests(
//            @Parameter(description = "Supplier ID to view requests", required = true)
//            @RequestBody SupplierIDDTO supplierIDDTO) {
//        return supplierService.viewRequests(supplierIDDTO);
//    }
//
//    @Operation(
//            summary = "View a single request for a supplier",
//            description = "Retrieves details of a single booking request for a supplier. Requires JWT authentication.",
//            security = @SecurityRequirement(name = "JWT")
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Booking request retrieved successfully",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid supplier ID or request ID", content = @Content),
//            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
//    })
//    @GetMapping("/single-request")
//    public Booking viewSingleRequest(
//            @Parameter(description = "Supplier ID to view a single request", required = true)
//            @RequestBody SupplierIDDTO supplierIDDTO) {
//        return supplierService.viewSingleRequest(supplierIDDTO);
//    }
//
//    @Operation(
//            summary = "Create an offer",
//            description = "Creates a new offer for a booking request. Requires JWT authentication.",
//            security = @SecurityRequirement(name = "JWT")
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Offer created successfully",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Offer.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid offer data", content = @Content),
//            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
//    })
//    @PostMapping("/create-offer")
//    public Offer createOffer(
//            @Parameter(description = "Offer details", required = true)
//            @RequestBody OfferRequestDTO offerRequestDTO) {
//        return supplierService.createOffer(offerRequestDTO);
//    }
//
//    @Operation(
//            summary = "Accept a booking request",
//            description = "Accepts a booking request by ID. Requires JWT authentication.",
//            security = @SecurityRequirement(name = "JWT")
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Booking request accepted successfully",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid request ID", content = @Content),
//            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
//    })
//    @PutMapping("/accept-request")
//    public Booking acceptBooking(
//            @Parameter(description = "Request ID to accept", required = true)
//            @RequestBody RequestBodyID requestBodyID) {
//        return supplierService.acceptRequest(requestBodyID.getId());
//    }
//
//    @Operation(
//            summary = "Get all suppliers",
//            description = "Retrieves a paginated list of all suppliers. Requires JWT authentication.",
//            security = @SecurityRequirement(name = "JWT")
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "List of suppliers retrieved successfully",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
//            ),
//            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
//    })
//    @GetMapping("/all-suppliers")
//    public Page<Provider> getAllSuppliers(
//            @Parameter(description = "Pagination and sorting parameters (e.g., page=0, size=10, sort=name,asc)")
//            Pageable pageable) {
//        return supplierService.getAllSuppliers(pageable);
//    }
//}