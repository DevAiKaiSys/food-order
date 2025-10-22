package com.example.food_order.controller;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.OrderItemRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.dto.response.OrderDetailResponse.CustomerInfo;
import com.example.food_order.dto.response.OrderDetailResponse.OrderItemInfo;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;  // mock OrderService

    @InjectMocks
    private OrderController orderController;  // inject mock OrderService เข้าไปใน OrderController

    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrderRequest createOrderRequest;
    private OrderDetailResponse orderDetailResponse;

    @BeforeEach
    void setUp() {
        // Mock data
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setMenuName("Burger");
        orderItemRequest.setQty(1);
        orderItemRequest.setPrice(BigDecimal.valueOf(100));
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomer("John Doe");
        createOrderRequest.setPhone("0812345678");
        createOrderRequest.setItems(Collections.singletonList(orderItemRequest));

        CustomerInfo customerInfo = CustomerInfo.builder()
                .id(1L)
                .name("John Doe")
                .phone("0812345678")
                .build();

        OrderItemInfo orderItemInfo = OrderItemInfo.builder()
                .menuName("Burger")
                .quantity(1)
                .price(BigDecimal.valueOf(100))
                .total(BigDecimal.valueOf(100))
                .build();

        orderDetailResponse = OrderDetailResponse.builder()
                .id(1L)
                .customer(customerInfo)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100))
                .items(Collections.singletonList(orderItemInfo))
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .build();
    }

    @Test
    void createOrder_shouldReturnCreatedOrder() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(orderDetailResponse);  // ทำให้ mock ค่าผลลัพธ์จาก service

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status_code").value("MDB-200"))
                .andExpect(jsonPath("$.status_msg").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.customer.name").value("John Doe"))
                .andExpect(jsonPath("$.data.total_amount").value(100.0));
    }
}
