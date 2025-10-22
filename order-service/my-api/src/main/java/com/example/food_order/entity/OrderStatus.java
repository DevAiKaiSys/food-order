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
            return this != COMPLETED;
        }

        return switch (this) {
            case PENDING -> newStatus == CONFIRMED;
            case CONFIRMED -> newStatus == COOKING;
            case COOKING -> newStatus == DELIVERING;
            case DELIVERING -> newStatus == COMPLETED;
            default -> false;
        };
    }

    public OrderStatus getNextStatus() {
        return switch (this) {
            case PENDING -> CONFIRMED;
            case CONFIRMED -> COOKING;
            case COOKING -> DELIVERING;
            case DELIVERING -> COMPLETED;
            default -> this;
        };
    }
}