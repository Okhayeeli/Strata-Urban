package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.RequestEntities.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogisticsRequestRepository extends JpaRepository<BookingRequest, Long> {}
