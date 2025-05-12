package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // Find a user by username or email
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Search users by name or email
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " + "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(String query);

    // Find users by role
    List<User> findByRoles(EnumRoles role);
//
//    // Find providers by service area ID
//    @Query("SELECT p FROM Provider p JOIN p.serviceAreas sa WHERE sa.id = :serviceAreaId")
//    List<User> findProvidersByServiceAreaId(Long serviceAreaId);

    @Query("SELECT u FROM User u WHERE u.serviceAreas LIKE %:serviceAreaId% AND TYPE(u) = Provider")
    List<User> findProvidersByServiceAreaId(@Param("serviceAreaId") String serviceAreaId);

    @Query("SELECT u FROM User u WHERE u.serviceAreas LIKE %:serviceAreaName% AND TYPE(u) = Provider")
    List<User> findProvidersByServiceAreaName(@Param("serviceAreaName") String serviceAreaName);

//    Optional<User> findByCurrentRefreshTokenJti(String jti);

}