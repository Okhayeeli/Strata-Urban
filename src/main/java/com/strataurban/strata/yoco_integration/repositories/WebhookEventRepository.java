package com.strataurban.strata.yoco_integration.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.strataurban.strata.yoco_integration.entities.WebhookEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<WebhookEvent> findByCheckoutId(String checkoutId);

    List<WebhookEvent> findByProcessedFalse();

    List<WebhookEvent> findByProcessedFalseAndReceivedAtBefore(LocalDateTime beforeDate);

    List<WebhookEvent> findByEventType(String eventType);
}
