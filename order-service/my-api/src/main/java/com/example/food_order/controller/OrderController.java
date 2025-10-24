package com.example.food_order.controller;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.dto.response.OrderResponse;
import com.example.food_order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 1. สร้างออเดอร์ใหม่
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    // 2. ค้นหาและแบ่งหน้า
    // (Spring Boot จะแปลง query params ?page=0&size=10 เป็น Pageable อัตโนมัติ)
    @GetMapping
    public Page<OrderResponse> searchOrders(
            Pageable pageable,  // การแบ่งหน้า
            @RequestParam(required = false) String searchId // ค่าพารามิเตอร์ searchId
    ) {
        return orderService.searchOrders(searchId, pageable);
    }
}
