package com.example.food_order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    private Long id;
    @NotEmpty(message = "Menu Name is required")
    @NotNull(message = "Menu Name is required")
    private String menuName;
    @NotNull(message = "Menu Price is required")
    private BigDecimal price;
    private String image;
    private String category;
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private int qty;
}