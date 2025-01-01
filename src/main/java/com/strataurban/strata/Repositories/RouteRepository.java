package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.Providers.Routes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.RouteMatcher;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Routes, Long> {

    List<Routes> findAllByCountry(String country);
    List<Routes> findAllByCountryAndState(String country, String state);
    List<Routes> findAllByCountryAndStateAndCity(String country, String state, String city);
}
