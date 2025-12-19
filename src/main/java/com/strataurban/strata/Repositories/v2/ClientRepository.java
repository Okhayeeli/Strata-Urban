package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Passengers.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    Optional<Client> findByEmailIgnoreCase(String email);

    Client findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByEmailVerified(Boolean emailVerified);

    List<Client> findByCityIgnoreCase(String city);

    List<Client> findByStateIgnoreCase(String state);
}