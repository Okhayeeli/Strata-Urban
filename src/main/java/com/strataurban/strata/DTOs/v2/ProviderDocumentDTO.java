package com.strataurban.strata.DTOs.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for Provider Documents")
public class ProviderDocumentDTO {

    @Schema(description = "Unique identifier of the provider document", example = "doc123")
    private String id;

    @Schema(description = "URL of the provider registration document", example = "https://example.com/docs/registration.pdf")
    private String providerRegistrationDocument;

    @Schema(description = "URL of the provider license document", example = "https://example.com/docs/license.pdf")
    private String providerLicenseDocument;

    @Schema(description = "URL of the provider name document", example = "https://example.com/docs/name.pdf")
    private String providerNameDocument;

    @Schema(description = "URL of the tax document", example = "https://example.com/docs/tax.pdf")
    private String taxDocument;
}