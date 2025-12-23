package com.strataurban.strata.Entities.Providers;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Table
@Entity
public class Routes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String start;
    @Column
    private String end;
    @Column
    private BigDecimal price;
    @Column
    private String providerId;
    @Column
    private String state;
    @Column
    private String country;
    @Column
    private String city;
    @Column
    private Boolean isEnabled;

    @Transient
    public List<String> getProviderIdList() {
        if (providerId == null || providerId.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return Arrays.stream(providerId.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(java.util.ArrayList::new));
    }

    @Transient
    public void setProviderIdList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            this.providerId = null;
        } else {
            this.providerId = ids.stream()
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.joining(","));
        }
    }

    @Transient
    public void addProviderId(String providerId) {
        if (providerId == null || providerId.trim().isEmpty()) return;

        List<String> current = getProviderIdList();
        if (!current.contains(providerId.trim())) {
            current.add(providerId.trim());
            setProviderIdList(current);
        }
    }

    @Transient
    public void removeProviderId(String providerId) {
        if (providerId == null) return;
        List<String> current = getProviderIdList();
        current.remove(providerId.trim());
        setProviderIdList(current);
    }
}
