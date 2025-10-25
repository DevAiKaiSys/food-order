package com.example.food_order.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    COOKING,
    DELIVERING,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus newStatus) {
        if (this == CANCELLED || this == COMPLETED) {
            return false;
        }

        if (newStatus == CANCELLED) {
            return true;
        }

        return switch (this) {
            case PENDING -> newStatus == CONFIRMED;
            case CONFIRMED -> newStatus == COOKING;
            case COOKING -> newStatus == DELIVERING;
            case DELIVERING -> newStatus == COMPLETED;
            default -> false;
        };
    }
}