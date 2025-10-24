package com.example.food_order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotEmpty(message = "Customer Name is required")
    @NotNull(message = "Customer Name is required")
    private String customerName;
    @NotEmpty(message = "Phone is required")
    @NotNull(message = "Phone is required")
    private String phone;
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> details;
    @NotNull(message = "Total price is required")
    private BigDecimal totalAmount;
}