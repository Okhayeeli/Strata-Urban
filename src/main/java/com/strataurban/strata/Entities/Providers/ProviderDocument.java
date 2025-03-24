package com.strataurban.strata.Entities.Providers;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.C;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
public class ProviderDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column
    private Long providerId; // Added to link to the provider

    @Column
    private String providerRegistrationDocument;

    @Column
    private String providerLicenseDocument;

    @Column
    private String providerNameDocument;

    @Column
    private String taxDocument;
}