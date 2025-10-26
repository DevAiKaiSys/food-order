package com.example.food_order.relay;

import com.example.food_order.entity.OutboxEvent;
import com.example.food_order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final StringRedisTemplate stringRedisTemplate; // send to Redis Stream

    private static final String ORDER_STREAM_KEY = "order_events_stream";
    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void pollAndPublishEvents() {
//        log.info("Polling for unpublished outbox events...");
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);

        List<OutboxEvent> events = outboxEventRepository
                .findByPublishedAtIsNullOrderByCreatedAtAsc(pageable);

        if (events.isEmpty()) {
            return;
        }

        log.info("Polling for unpublished outbox events... Found {} events.", events.size());

        for (OutboxEvent event : events) {
            try {
                log.info("[EVENT_LOG] Publishing event: {}", event.getPayload());

                Map<String, String> eventData = Map.of(
                        "eventId", event.getId().toString(),
                        "aggregateId", event.getAggregateId(),
                        "eventType", event.getEventType(),
                        "payload", event.getPayload()
                );

                stringRedisTemplate.opsForStream().add(ORDER_STREAM_KEY, eventData);
                log.info("Successfully published event {} to Redis Stream: {}",
                        event.getId(), ORDER_STREAM_KEY);

                event.setPublishedAt(OffsetDateTime.now());
                outboxEventRepository.save(event);

            } catch (Exception e) {
                log.error("Failed to publish event {}: {}. Will retry later.", event.getId(), e.getMessage());
            }
        }
    }
}