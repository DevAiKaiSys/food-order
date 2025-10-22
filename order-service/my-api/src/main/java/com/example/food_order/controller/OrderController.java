package com.example.food_order.controller;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.response.ApiResponse;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.service.OrderService;
import com.example.food_order.util.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(
             @RequestBody CreateOrderRequest request) {
        String spanId = DataUtil.generateSpanId();
        OrderDetailResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, spanId));
    }
}
