package com.example.food_order.dto.response;

import com.example.food_order.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderDetailResponse {

    private Long id;

    @JsonProperty("customer")
    private CustomerInfo customer;

    private OrderStatus status;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("items")
    private List<OrderItemInfo> items;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("cancelled_at")
    private String cancelledAt;

    @Data
    @Builder
    public static class CustomerInfo {
        private Long id;
        private String name;
        private String phone;
    }

    @Data
    @Builder
    public static class OrderItemInfo {
        @JsonProperty("menu_name")
        private String menuName;

        private Integer quantity;
        private BigDecimal price;
        private BigDecimal total;
    }
}
