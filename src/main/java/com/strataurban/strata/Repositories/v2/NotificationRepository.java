package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Generics.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a specific user (recipient)
    List<Notification> findByRecipientId(Long recipientId);
}