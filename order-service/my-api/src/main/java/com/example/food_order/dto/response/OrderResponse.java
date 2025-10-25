package com.example.food_order.dto.response;

import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;

    private String slipId;

    private String customerName;

    private String phone;

    private int itemCount;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private String createdAt;

    private String updatedAt;

    // Helper method สำหรับแปลง Entity เป็น DTO
    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .slipId(order.getSlipId())
                .customerName(order.getCustomer().getName())
                .phone(order.getCustomer().getPhone())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .itemCount(order.getItems().size())
                .createdAt(DateTimeUtil.formatZoneDateTime(order.getCreatedAt()))
                .updatedAt(DateTimeUtil.formatZoneDateTime(order.getUpdatedAt()))
                .build();
    }
}