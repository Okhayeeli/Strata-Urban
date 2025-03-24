package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to contact a party involved in a booking")
public class ContactRequest {

    @Schema(description = "Method of contact", example = "EMAIL")
    private String contactMethod; // e.g., EMAIL, PHONE, IN_APP_CHAT

    @Schema(description = "Message to send", example = "Please confirm the pickup time.")
    private String message;
}