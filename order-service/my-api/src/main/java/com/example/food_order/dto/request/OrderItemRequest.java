package com.example.food_order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    private Long id;
    private String menuName;
    private BigDecimal price;
    private String image;
    private String category;
    private int qty;
}