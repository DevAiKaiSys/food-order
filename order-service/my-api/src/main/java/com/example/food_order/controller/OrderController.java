package com.example.food_order.controller;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.UpdateOrderStatusRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.dto.response.OrderResponse;
import com.example.food_order.dto.response.PageResponse;
import com.example.food_order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public PageResponse<OrderResponse> searchOrders(
            Pageable pageable,  // การแบ่งหน้า
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchId // ค่าพารามิเตอร์ searchId
    ) {
        return orderService.searchOrders(pageable, status, searchId);
    }

    // 3. รายละเอียดออเดอร์
    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrderDetails(@PathVariable Long orderId) {
        return orderService.getOrderDetails(orderId);
    }

    // 4. ยืนยันออเดอร์
    @PostMapping("/{orderId}/confirm")
    public OrderDetailResponse confirmOrder(@PathVariable Long orderId) {
        return orderService.confirmOrder(orderId);
    }

    // 5. เริ่มทำอาหาร
    @PostMapping("/{orderId}/cook")
    public OrderDetailResponse cookOrder(@PathVariable Long orderId) {
        return orderService.startCooking(orderId);
    }

    // 6. กำลังจัดส่ง
    @PostMapping("/{orderId}/deliver")
    public OrderDetailResponse deliverOrder(@PathVariable Long orderId) {
        return orderService.startDelivering(orderId);
    }

    // 7. เสร็จสิ้น
    @PostMapping("/{orderId}/complete")
    public OrderDetailResponse completeOrder(@PathVariable Long orderId) {
        return orderService.completeOrder(orderId);
    }

    // 8. ยกเลิก
    @PostMapping("/{orderId}/cancel")
    public OrderDetailResponse cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    @PatchMapping("/{orderId}/status")
    public OrderDetailResponse updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(orderId, request);
    }
}
