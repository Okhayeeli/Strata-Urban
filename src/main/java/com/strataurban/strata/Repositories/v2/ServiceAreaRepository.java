package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.ServiceArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceAreaRepository extends JpaRepository<ServiceArea, Long> {
}