package com.strataurban.strata.DTOs.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for simplified receipt summary (for lists)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptSummaryDTO {
    private Long id;
    private String receiptNumber;
    private LocalDateTime generatedAt;
    private String bookingReference;
    private BigDecimal totalAmount;
    private String currency;
    private String clientName;
    private String providerName;
    private String serviceDescription;
    private LocalDateTime serviceDate;
    private String status;
}