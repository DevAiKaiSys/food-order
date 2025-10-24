package com.example.food_order.service;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.OrderItemRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.entity.Customer;
import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.repository.CustomerRepository;
import com.example.food_order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest createOrderRequest;
    private Customer customer;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setPhone("1234567890");

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerName("Test Customer");
        createOrderRequest.setPhone("1234567890");
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setMenuName("Test Menu");
        orderItemRequest.setQty(1);
        orderItemRequest.setPrice(BigDecimal.valueOf(100));
        createOrderRequest.setItems(Collections.singletonList(orderItemRequest));

        order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100))
                .build();
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
    void createOrder_whenExistingCustomer_shouldCreateOrderSuccessfully() {
        when(customerRepository.findByPhone("1234567890")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDetailResponse response = orderService.createOrder(createOrderRequest);

        assertNotNull(response);
        assertEquals(order.getId(), response.getId());
        assertEquals(customer.getName(), response.getCustomer().getName());
        assertEquals(order.getTotalAmount(), response.getTotalAmount());
    }
}
