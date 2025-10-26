package com.example.food_order.service;

import com.example.food_order.dto.response.OrderResponse;
import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.entity.OutboxEvent;
import com.example.food_order.enums.OrderEventType;
import com.example.food_order.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void createOrderStatusChangedEvent(Order updatedOrder) {
        try {
            String payload = objectMapper.writeValueAsString(OrderResponse.fromEntity(updatedOrder));

            OrderEventType eventType = determineEventType(updatedOrder.getStatus());

            OutboxEvent event = new OutboxEvent(
                    "ORDER",
                    updatedOrder.getId().toString(),
                    eventType.name(),
                    payload
            );

            outboxEventRepository.save(event);

//            log.info("Created outbox event for Order ID: {}", updatedOrder.getId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order DTO for outbox event: {}", e.getMessage());
            throw new RuntimeException("Failed to create outbox event payload", e);
        }
    }

    private OrderEventType determineEventType(OrderStatus status) {
        return switch (status) {
            case PENDING -> OrderEventType.ORDER_CREATED;
            case COMPLETED -> OrderEventType.ORDER_COMPLETED;
            case CANCELLED -> OrderEventType.ORDER_CANCELLED;
            default -> OrderEventType.ORDER_STATUS_CHANGED;
        };
    }
}