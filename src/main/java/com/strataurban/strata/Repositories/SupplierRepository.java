package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.Providers.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

}
