package com.example.food_order.dto.response;

import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.util.DateTimeUtil;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderDetailResponse {

    private Long id;

    private String slipId;

    private CustomerInfo customer;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private List<OrderItemInfo> items;

    private String createdAt;

    private String updatedAt;

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
        private String menuName;

        private Integer quantity;
        private BigDecimal price;
        private BigDecimal total;
    }

    // Helper method สำหรับแปลง Entity เป็น DTO
    public static OrderDetailResponse fromEntity(Order order) {
        List<OrderItemInfo> orderItems = order.getItems().stream()
                .map(item -> OrderItemInfo.builder()
                        .menuName(item.getMenuName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .total(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderDetailResponse.builder()
                .id(order.getId())
                .slipId(order.getSlipId())
                .customer(CustomerInfo.builder()
                        .id(order.getCustomer().getId())
                        .name(order.getCustomer().getName())
                        .phone(order.getCustomer().getPhone())
                        .build())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(orderItems)
                .createdAt(DateTimeUtil.formatZoneDateTime(order.getCreatedAt()))
                .updatedAt(DateTimeUtil.formatZoneDateTime(order.getUpdatedAt()))
                .build();
    }
}
