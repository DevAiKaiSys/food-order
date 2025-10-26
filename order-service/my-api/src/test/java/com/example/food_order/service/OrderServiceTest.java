package com.example.food_order.service;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.OrderItemRequest;
import com.example.food_order.dto.request.UpdateOrderStatusRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.entity.Customer;
import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.exception.InvalidStateException;
import com.example.food_order.exception.ResourceNotFoundException;
import com.example.food_order.repository.CustomerRepository;
import com.example.food_order.repository.OrderRepository;
import com.example.food_order.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderRequest createOrderRequest;
    private Customer customer;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = createCustomer();

        createOrderRequest = createOrderRequest(customer);

        order = createOrder(customer);

        // ตั้งค่า timestamps ด้วย TestUtil
        TestUtil.setTimestamps(order);
    }

    private Customer createCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setPhone("1234567890");
        return customer;
    }

    private CreateOrderRequest createOrderRequest(Customer customer) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName(customer.getName());
        request.setPhone(customer.getPhone());

        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setMenuName("Test Menu");
        orderItemRequest.setQty(1);
        orderItemRequest.setPrice(BigDecimal.valueOf(100));

        request.setDetails(Collections.singletonList(orderItemRequest));
        return request;
    }

    private Order createOrder(Customer customer) {
        return Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100))
                .build();
    }

    @Test
    void createOrder_shouldSetTimestampsAutomatically() {
        mockOrderRepository();

        // ทดสอบว่า Order มี timestamps ที่ถูกต้อง
        OrderDetailResponse response = orderService.createOrder(createOrderRequest);

        Order savedOrder = orderRepository.findById(response.getId()).orElseThrow();
        assertNotNull(savedOrder.getCreatedAt());
        assertNotNull(savedOrder.getUpdatedAt());
        assertEquals(savedOrder.getCreatedAt(), savedOrder.getUpdatedAt());
    }

    @Test
    void createOrder_whenNewCustomer_shouldCreateOrderSuccessfully() {
        when(customerRepository.findByPhone("1234567890")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDetailResponse response = orderService.createOrder(createOrderRequest);

        assertNotNull(response);
        assertEquals(order.getId(), response.getId());
        assertEquals(customer.getName(), response.getCustomer().getName());
        assertEquals(order.getTotalAmount(), response.getTotalAmount());
    }

    @Test
    void getOrderDetails_whenOrderNotFound_shouldThrowResourceNotFoundException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderDetails(1L));
    }

    @Test
    void updateStatus_whenOrderNotFound_shouldThrowResourceNotFoundException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.confirmOrder(1L));
    }

    @Test
    void updateStatus_whenTransitioningFromPendingToCooking_shouldThrowInvalidStateException() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStateException.class, () -> orderService.startCooking(1L));
    }

    @Test
    void updateStatus_whenTransitioningFromCookingToCompleted_shouldThrowInvalidStateException() {
        order.setStatus(OrderStatus.COOKING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStateException.class, () -> orderService.completeOrder(1L));
    }

    @Test
    void updateStatus_whenTransitioningFromCookingToConfirmed_shouldThrowInvalidStateException() {
        order.setStatus(OrderStatus.COOKING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        assertThrows(InvalidStateException.class, () -> orderService.updateOrderStatus(1L, request));
    }

    @Test
    void updateStatus_whenTransitioningFromDeliveringToConfirmed_shouldThrowInvalidStateException() {
        order.setStatus(OrderStatus.DELIVERING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        assertThrows(InvalidStateException.class, () -> orderService.updateOrderStatus(1L, request));
    }

    @Test
    void updateStatus_whenCancellingCompletedOrder_shouldThrowInvalidStateException() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStateException.class, () -> orderService.cancelOrder(1L));
    }

    private void mockOrderRepository() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderRepository.findById(any(Long.class))).thenReturn(Optional.of(order));
    }
}