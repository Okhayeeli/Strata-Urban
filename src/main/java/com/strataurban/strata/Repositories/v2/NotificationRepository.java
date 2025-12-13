package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Generics.Notification;
import com.strataurban.strata.Notifications.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.strataurban.strata.Enums.NotificationType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a specific user (recipient)
    Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);

    // Count unread notifications for a user
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Find unread notifications for a user
    Page<Notification> findByRecipientIdAndIsReadFalse(Long recipientId, Pageable pageable);

    // Find notifications by type
    Page<Notification> findByRecipientIdAndType(Long recipientId, NotificationType type, Pageable pageable);

    // Find notifications by channel
    Page<Notification> findByRecipientIdAndChannel(Long recipientId, NotificationChannel channel, Pageable pageable);

    // Find notifications by reference
    List<Notification> findByReferenceId(Long referenceId);

    // Find recent notifications (within last N days)
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId " +
            "AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    Page<Notification> findRecentNotifications(
            @Param("recipientId") Long recipientId,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    // Find failed notifications for retry
    @Query("SELECT n FROM Notification n WHERE n.deliveryStatus = 'FAILED' " +
            "AND n.createdAt >= :since")
    List<Notification> findFailedNotifications(@Param("since") LocalDateTime since);

    // Delete old read notifications (for cleanup)
    @Query("DELETE FROM Notification n WHERE n.recipientId = :recipientId " +
            "AND n.isRead = true AND n.createdAt < :before")
    void deleteOldReadNotifications(
            @Param("recipientId") Long recipientId,
            @Param("before") LocalDateTime before);

    // Get notification statistics
    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
            "WHERE n.recipientId = :recipientId " +
            "AND n.createdAt >= :since " +
            "GROUP BY n.type")
    List<Object[]> getNotificationStatistics(
            @Param("recipientId") Long recipientId,
            @Param("since") LocalDateTime since);
}