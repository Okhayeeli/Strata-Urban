package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.Passengers.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllBySupplierId(Long supplierId);
}
