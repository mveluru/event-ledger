
package com.ledger.gateway.repository;

import com.ledger.gateway.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    Optional<EventEntity> findByEventId(String eventId);
    List<EventEntity> findByAccountIdOrderByEventTimestampAsc(String accountId);
}
