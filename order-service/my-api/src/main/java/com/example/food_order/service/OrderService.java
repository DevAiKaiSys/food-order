package com.example.food_order.service;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.OrderItemRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.dto.response.OrderResponse;
import com.example.food_order.entity.Customer;
import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderItem;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.repository.CustomerRepository;
import com.example.food_order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.food_order.util.DateTimeUtil.MY_TIMEZONE;

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

        generateSlipId(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getDetails()) {

            OrderItem orderItem = OrderItem.builder()
                    .menuName(itemReq.getMenuName())
                    .quantity(itemReq.getQty())
                    .price(itemReq.getPrice())
                    .build();

            orderItem.calculateSubtotal();
            totalAmount = totalAmount.add(orderItem.getSubtotal());
            order.addItem(orderItem);
        }

        order.calculateTotalAmount();

        // Cross-check TotalAmount
        if (!totalAmount.equals(order.getTotalAmount())) {
            // TODO: something
        }

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return OrderDetailResponse.fromEntity(savedOrder);
    }

    private void generateSlipId(Order order) {
        // ดึงวันที่ในรูปแบบ yyMMdd จาก BKK timezone
        String datePart = ZonedDateTime.now(MY_TIMEZONE).format(DateTimeFormatter.ofPattern("yyMMdd"));

        // คำนวณช่วงเวลา startDate และ endDate ใน UTC
        ZonedDateTime startDate = ZonedDateTime.now(MY_TIMEZONE)
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
                .withZoneSameInstant(ZoneOffset.UTC); // เปลี่ยนให้เป็น UTC
        ZonedDateTime endDate = ZonedDateTime.now(MY_TIMEZONE)
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999)
                .withZoneSameInstant(ZoneOffset.UTC); // เปลี่ยนให้เป็น UTC

        // ดึงหมายเลขลำดับจากฐานข้อมูล (หรือใช้ AtomicLong)
        Long orderCount = orderRepository.countOrdersByDate(startDate, endDate);

        // หรือ ค้นหาจำนวนออเดอร์ที่มี slipId ที่มี prefix "ORD-" และวันที่ตรงกัน
//        Long orderCount = orderRepository.countOrdersBySlipIdPrefix("ORD-" + datePart);

        // สร้าง slipId โดยการเพิ่มหมายเลขลำดับเข้าไป
        String sequencePart = String.format("%07d", orderCount + 1); // เพิ่มเลขจาก 0000001 เป็นต้นไป

        // เพิ่ม "ORD-" เป็น prefix
        order.setSlipId("ORD-" + datePart + sequencePart);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> searchOrders(String searchId, Pageable pageable) {
        log.info("Searching orders with searchId: {}", searchId);

        // ถ้ามี searchId, ค้นหาจาก Order ID ที่มีส่วนประกอบของ searchId พร้อมกับการเรียงลำดับแบบ Custom
        Page<Order> orderPage;
        if (searchId != null && !searchId.isEmpty()) {
            // ค้นหาตาม searchId พร้อมการเรียงลำดับ Custom
            orderPage = orderRepository.findByIdContainingOrderByStatusCustom(searchId, pageable);
        } else {
            // ถ้าไม่มี searchId, ค้นหาทุกคำสั่งและจัดเรียงตามสถานะและวันที่
            orderPage = orderRepository.findAllByOrderByStatusAscCreatedAtDesc(pageable);
        }

        // เปลี่ยนจาก Order -> OrderResponse
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(orderResponses, pageable, orderPage.getTotalElements());
    }
}

