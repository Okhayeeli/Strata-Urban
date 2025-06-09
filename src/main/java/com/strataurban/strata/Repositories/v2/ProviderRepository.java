package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    // Search providers by service type, city, and minimum rating
    @Query("SELECT p FROM Provider p WHERE " +
            "(:serviceType IS NULL OR :serviceType MEMBER OF p.serviceTypes) AND " +
            "(:city IS NULL OR p.city = :city) AND " +
            "(:minRating IS NULL OR p.rating >= :minRating)")
    List<Provider> searchProviders(String serviceType, String city, Double minRating);


    @Query("SELECT p FROM Provider p WHERE " +
            "(:name IS NULL OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:serviceType IS NULL OR :serviceType MEMBER OF p.serviceTypes) AND " +
            "(:city IS NULL OR LOWER(p.city) = LOWER(:city)) AND " +
            "(:minRating IS NULL OR p.rating >= :minRating)")
    Page<Provider> findByCriteria(@Param("name") String name,
                                  @Param("serviceType") String serviceType,
                                  @Param("city") String city,
                                  @Param("minRating") Double minRating,
                                  Pageable pageable);
}