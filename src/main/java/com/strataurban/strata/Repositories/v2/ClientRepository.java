package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Passengers.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Enhanced Client Repository with specification support for advanced filtering
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    /**
     * Find client by email address
     * @param email the email to search for
     * @return Optional containing the client if found
     */
    Optional<Client> findByEmailIgnoreCase(String email);

    /**
     * Find client by email address (returns null if not found)
     * Useful for duplicate checking
     * @param email the email to search for
     * @return Client or null
     */
    Client findByEmail(String email);

    /**
     * Check if email exists
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Count clients by verification status
     * @param emailVerified the verification status
     * @return count of clients
     */
    long countByEmailVerified(Boolean emailVerified);

    /**
     * Find clients by city
     * @param city the city name
     * @return list of clients
     */
    java.util.List<Client> findByCityIgnoreCase(String city);

    /**
     * Find clients by state
     * @param state the state name
     * @return list of clients
     */
    java.util.List<Client> findByStateIgnoreCase(String state);
}