package com.example.food_order.service;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.UpdateOrderStatusRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.dto.response.OrderResponse;
import com.example.food_order.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderDetailResponse createOrder(CreateOrderRequest request);

    PageResponse<OrderResponse> searchOrders(Pageable pageable, String status, String searchId);

    OrderDetailResponse getOrderDetails(Long orderId);

    // Action Methods
    OrderDetailResponse confirmOrder(Long orderId);

    OrderDetailResponse startCooking(Long orderId);

    OrderDetailResponse startDelivering(Long orderId);

    OrderDetailResponse completeOrder(Long orderId);

    OrderDetailResponse cancelOrder(Long orderId);

    OrderDetailResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}

