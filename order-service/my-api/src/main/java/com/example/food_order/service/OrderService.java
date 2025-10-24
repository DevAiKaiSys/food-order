package com.example.food_order.service;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.OrderItemRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.entity.Customer;
import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderItem;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.repository.CustomerRepository;
import com.example.food_order.repository.OrderRepository;
import com.example.food_order.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrderDetailResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer: {} phone: {}", request.getCustomerName(), request.getPhone());

        // หา customer จากเบอร์โทร
        Customer customer = customerRepository.findByPhone(request.getPhone())
                .map(existing -> {
                    existing.setName(request.getCustomerName());
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setName(request.getCustomerName());
                    newCustomer.setPhone(request.getPhone());
                    return customerRepository.save(newCustomer);
                });

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {

            OrderItem orderItem = OrderItem.builder()
                    .menuName(itemReq.getMenuName())
                    .quantity(itemReq.getQty())
                    .price(itemReq.getPrice())
                    .build();

            orderItem.calculateSubtotal();
            totalAmount = totalAmount.add(orderItem.getSubtotal());
            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return mapToDetailResponse(savedOrder);
    }

    private OrderDetailResponse mapToDetailResponse(Order order) {
        List<OrderDetailResponse.OrderItemInfo> orderItems = order.getItems().stream()
                .map(item -> OrderDetailResponse.OrderItemInfo.builder()
                        .menuName(item.getMenuName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .total(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderDetailResponse.builder()
                .id(order.getId())
                .customer(OrderDetailResponse.CustomerInfo.builder()
                        .id(order.getCustomer().getId())
                        .name(order.getCustomer().getName())
                        .phone(order.getCustomer().getPhone())
                        .build())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(orderItems)
                .createdAt(DateTimeUtil.formatDateTime(order.getCreatedAt()))
                .updatedAt(DateTimeUtil.formatDateTime(order.getUpdatedAt()))
                .build();
    }
}

