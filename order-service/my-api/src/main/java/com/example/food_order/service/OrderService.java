package com.example.food_order.service;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.UpdateOrderStatusRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderDetailResponse createOrder(CreateOrderRequest request);

    Page<OrderResponse> searchOrders(String searchId, Pageable pageable);

    OrderDetailResponse getOrderDetails(Long orderId);

    // Action Methods
    OrderDetailResponse confirmOrder(Long orderId);

    OrderDetailResponse startCooking(Long orderId);

    OrderDetailResponse startDelivering(Long orderId);

    OrderDetailResponse completeOrder(Long orderId);

    OrderDetailResponse cancelOrder(Long orderId);

    OrderDetailResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}

