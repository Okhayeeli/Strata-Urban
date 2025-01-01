package com.strataurban.strata.Entities.Generics;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String role;
    @Column
    private String description;
}
