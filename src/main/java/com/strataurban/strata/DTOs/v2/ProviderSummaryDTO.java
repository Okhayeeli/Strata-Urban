package com.strataurban.strata.DTOs.v2;

import lombok.Data;

@Data
public class ProviderSummaryDTO {
    private String providerId;
    private String companyName;
    private String companyLogoUrl;
    private String companyBusinessPhone;
    private String companyBusinessEmail;
    private Double rating;

    public ProviderSummaryDTO(String providerId, String companyName, String companyLogoUrl,
                              String companyBusinessPhone, String companyBusinessEmail, Double rating) {
        this.providerId = providerId;
        this.companyName = companyName;
        this.companyLogoUrl = companyLogoUrl;
        this.companyBusinessPhone = companyBusinessPhone;
        this.companyBusinessEmail = companyBusinessEmail;
        this.rating = rating;
    }
}