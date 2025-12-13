package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Routes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Routes, Long> {

    List<Routes> findAllByCountryAndIsEnabledTrue(String country);
    List<Routes> findAllByCountryAndStateAndIsEnabledTrue(String country, String state);
    List<Routes> findAllByCountryAndStateAndCityAndIsEnabledTrue(String country, String state, String city);
    List<Routes> findByStartContainingAndEndContainingAndIsEnabledTrue(String startLocation, String endLocation);
    List<Routes> findAllByProviderIdAndIsEnabledTrue(String supplierId);
}
