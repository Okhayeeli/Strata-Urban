package com.strataurban.strata.Notifications.Repository;

import com.strataurban.strata.Notifications.Entities.NotificationPreference;
import com.strataurban.strata.Notifications.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    // Find all preferences for a user
    List<NotificationPreference> findByUserId(Long userId);

    // Find specific preference by user and channel
    Optional<NotificationPreference> findByUserIdAndChannel(Long userId, NotificationChannel channel);

    // Get all enabled channels for a user
    @Query("SELECT np.channel FROM NotificationPreference np " +
            "WHERE np.userId = :userId " +
            "AND np.enabled = true")
    List<NotificationChannel> findEnabledChannels(@Param("userId") Long userId);

    // Check if a specific channel is enabled
    @Query("SELECT CASE WHEN COUNT(np) > 0 THEN true ELSE false END " +
            "FROM NotificationPreference np " +
            "WHERE np.userId = :userId " +
            "AND np.channel = :channel " +
            "AND np.enabled = true")
    boolean isChannelEnabled(@Param("userId") Long userId, @Param("channel") NotificationChannel channel);

    // Delete all preferences for a user
    void deleteByUserId(Long userId);

    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.channel = :channel AND np.enabled = true")
    long countByChannelAndEnabledTrue(@Param("channel") NotificationChannel channel);
}