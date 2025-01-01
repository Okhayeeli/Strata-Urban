package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.Providers.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {}