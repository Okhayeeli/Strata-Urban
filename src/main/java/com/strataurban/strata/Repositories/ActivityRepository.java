package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.Generics.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
