package com.strataurban.strata.RestControllers.v2;

import com.strataurban.strata.Entities.Passengers.Receipt;
import com.strataurban.strata.ServiceImpls.v2.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
public class ReceiptRestController {

    private final ReceiptService receiptService;

    /**
     * Generate receipt after successful payment
     * POST /api/receipts/generate?paymentTransactionId=123
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReceipt(@RequestParam Long paymentTransactionId) {
        try {
            Receipt receipt = receiptService.generateReceipt(paymentTransactionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Receipt generated successfully");
            response.put("receipt", receipt);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error generating receipt for payment: {}", paymentTransactionId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to generate receipt");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get receipt by receipt number
     * GET /api/receipts/number/RCP-20250117-123456
     */
    @GetMapping("/number/{receiptNumber}")
    public ResponseEntity<Map<String, Object>> getReceiptByNumber(@PathVariable String receiptNumber) {
        return receiptService.getReceiptByNumber(receiptNumber).map(receipt -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("receipt", receipt);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Receipt not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }

    /**
     * Get receipt by payment transaction ID
     * GET /api/receipts/payment/123
     */
    @GetMapping("/payment/{paymentTransactionId}")
    public ResponseEntity<Map<String, Object>> getReceiptByPaymentId(@PathVariable Long paymentTransactionId) {
        return receiptService.getReceiptByPaymentId(paymentTransactionId).map(receipt -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("receipt", receipt);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Receipt not found for payment");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }

    /**
     * Get receipt by booking ID
     * GET /api/receipts/booking/123
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getReceiptByBookingId(@PathVariable Long bookingId) {
        return receiptService.getReceiptByBookingId(bookingId).map(receipt -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("receipt", receipt);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Receipt not found for booking");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }

    /**
     * Get all receipts for a client
     * GET /api/receipts/client/123
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Map<String, Object>> getClientReceipts(@PathVariable Long clientId) {
        List<Receipt> receipts = receiptService.getClientReceipts(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", receipts.size());
        response.put("receipts", receipts);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all receipts for a provider
     * GET /api/receipts/provider/123
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<Map<String, Object>> getProviderReceipts(@PathVariable Long providerId) {
        List<Receipt> receipts = receiptService.getProviderReceipts(providerId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", receipts.size());
        response.put("receipts", receipts);

        return ResponseEntity.ok(response);
    }

    /**
     * Get receipts by date range for a user
     * GET /api/receipts/date-range?userId=123&isClient=true&startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
     */
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getReceiptsByDateRange(@RequestParam Long userId, @RequestParam boolean isClient, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Receipt> receipts = receiptService.getReceiptsByDateRange(userId, isClient, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", receipts.size());
        response.put("receipts", receipts);
        response.put("dateRange", Map.of("start", startDate, "end", endDate));

        return ResponseEntity.ok(response);
    }

    /**
     * Void a receipt (for refunds/cancellations)
     * PUT /api/receipts/void/RCP-20250117-123456
     */
    @PutMapping("/void/{receiptNumber}")
    public ResponseEntity<Map<String, Object>> voidReceipt(@PathVariable String receiptNumber, @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "No reason provided");
            Receipt receipt = receiptService.voidReceipt(receiptNumber, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Receipt voided successfully");
            response.put("receipt", receipt);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error voiding receipt: {}", receiptNumber, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to void receipt");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Download receipt as PDF (placeholder for future implementation)
     * GET /api/receipts/download/RCP-20250117-123456
     */
    @GetMapping("/download/{receiptNumber}")
    public ResponseEntity<Map<String, Object>> downloadReceiptPDF(@PathVariable String receiptNumber) {
        // TODO: Implement PDF generation logic
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "PDF generation not yet implemented");
        response.put("receiptNumber", receiptNumber);

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }

    /**
     * Email receipt to client
     * POST /api/receipts/email/RCP-20250117-123456
     */
    @PostMapping("/email/{receiptNumber}")
    public ResponseEntity<Map<String, Object>> emailReceipt(@PathVariable String receiptNumber, @RequestBody(required = false) Map<String, String> request) {
        // TODO: Implement email sending logic
        String email = request != null ? request.get("email") : null;

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Email functionality not yet implemented");
        response.put("receiptNumber", receiptNumber);
        response.put("targetEmail", email);

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }
}
