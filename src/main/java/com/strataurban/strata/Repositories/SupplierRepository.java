package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.Providers.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Provider, Long> {

}
