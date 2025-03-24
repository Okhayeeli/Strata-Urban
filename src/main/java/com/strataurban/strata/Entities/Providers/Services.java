package com.strataurban.strata.Entities.Providers;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Services {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String serviceName; // e.g., Transport, Supplies Delivery, Gift Delivery, etc.
    @Column
    private Long providerId;
}
