package com.strataurban.strata.Entities.Providers;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table
public class SupplierDocuments {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
}
