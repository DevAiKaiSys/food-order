package com.example.food_order.exception;

import com.example.food_order.entity.OrderStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(OrderStatus from, OrderStatus to) {
        super(String.format("Cannot transition from %s to %s", from, to));
    }
}
