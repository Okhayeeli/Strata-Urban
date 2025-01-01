package com.strataurban.strata.Repositories;

import com.strataurban.strata.Entities.Generics.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Integer> {
}
