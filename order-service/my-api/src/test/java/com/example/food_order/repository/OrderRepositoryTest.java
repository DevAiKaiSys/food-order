package com.example.food_order.repository;

import com.example.food_order.entity.Customer;
import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testTimestampsWithAllFields() {
        // Create and save customer
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setPhone("1234567890");
        customer = customerRepository.save(customer);

        // Create order with all fields
        Order order = Order.builder()
                .customer(customer)
                .totalAmount(BigDecimal.valueOf(100))
                .status(OrderStatus.PENDING)
                .slipId("SLIP-" + System.currentTimeMillis())
                .spanId("SPAN-" + System.currentTimeMillis())
                .build();

        order = orderRepository.save(order);

        // Assertions
        assertNotNull(order.getId());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertNotNull(order.getSlipId());
        assertNotNull(order.getSpanId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(order.getTotalAmount()));
    }

    @Test
    void testOrderWithOnlySlipId() {
        // Test case สำหรับ order ที่มีแค่ slipId
        Customer customer = new Customer();
        customer.setName("Test Customer 3");
        customer.setPhone("1112223333");
        customer = customerRepository.save(customer);

        Order order = Order.builder()
                .customer(customer)
                .totalAmount(BigDecimal.valueOf(150))
                .status(OrderStatus.COOKING)
                .slipId("SLIP-12345")
                // ไม่ set spanId
                .build();

        order = orderRepository.save(order);

        assertNotNull(order.getId());
        assertEquals("SLIP-12345", order.getSlipId());
        assertNull(order.getSpanId());
        assertEquals(OrderStatus.COOKING, order.getStatus());
    }
}