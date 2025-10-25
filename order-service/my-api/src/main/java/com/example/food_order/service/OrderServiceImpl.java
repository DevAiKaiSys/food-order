package com.example.food_order.service;

import com.example.food_order.dto.request.CreateOrderRequest;
import com.example.food_order.dto.request.OrderItemRequest;
import com.example.food_order.dto.request.UpdateOrderStatusRequest;
import com.example.food_order.dto.response.OrderDetailResponse;
import com.example.food_order.dto.response.OrderResponse;
import com.example.food_order.dto.response.PageResponse;
import com.example.food_order.entity.Customer;
import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderItem;
import com.example.food_order.entity.OrderStatus;
import com.example.food_order.exception.InvalidStateException;
import com.example.food_order.exception.ResourceNotFoundException;
import com.example.food_order.repository.CustomerRepository;
import com.example.food_order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.example.food_order.util.DateTimeUtil.MY_TIMEZONE;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDetailResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer: {} phone: {}", request.getCustomerName(), request.getPhone());

        Customer customer = findOrCreateCustomer(request.getCustomerName(), request.getPhone());

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .build();

        generateSlipId(order);
        addOrderItemsFromRequest(order, request.getDetails());

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return OrderDetailResponse.fromEntity(savedOrder);
    }

    private Customer findOrCreateCustomer(String name, String phone) {
        return customerRepository.findByPhone(phone)
                .map(existing -> {
                    existing.setName(name);
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setName(name);
                    newCustomer.setPhone(phone);
                    return customerRepository.save(newCustomer);
                });
    }

    private void addOrderItemsFromRequest(Order order, List<OrderItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item.");
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : itemRequests) {
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

        // Cross-check TotalAmount, if they don't match, log a warning.
        if (!totalAmount.equals(order.getTotalAmount())) {
            log.warn("Calculated total amount {} does not match order's total amount {}. This might indicate an issue.", totalAmount, order.getTotalAmount());
        }
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

        // ดึงหมายเลขลำดับจากฐานข้อมูล (เฉพาะของวันนี้)
        Long orderCount = orderRepository.countOrdersByDate(startDate, endDate);

        // สร้าง slipId โดยการเพิ่มหมายเลขลำดับเข้าไป
        String sequencePart = String.format("%07d", orderCount + 1); // เพิ่มเลขจาก 0000001 เป็นต้นไป

        // เพิ่ม "ORD-" เป็น prefix
        order.setSlipId("ORD-" + datePart + sequencePart);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #searchId")
    public PageResponse<OrderResponse> searchOrders(Pageable pageable, String searchId) {
        if (searchId != null) {
            log.info("Searching orders with searchId: {}", searchId);
        }

        Page<Order> orderPage;
        if (searchId != null && !searchId.isEmpty()) {
            orderPage = orderRepository.findByIdContainingOrderByStatusCustom(searchId, pageable);
        } else {
            orderPage = orderRepository.findAllByOrderByStatusAscCreatedAtDesc(pageable);
        }

        List<OrderResponse> orderResponses = new ArrayList<>(
                orderPage.getContent().stream()
                        .map(OrderResponse::fromEntity)
                        .toList()
        );

        return PageResponse.<OrderResponse>builder()
                .content(orderResponses)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isLast(orderPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetails(Long orderId) {
        return orderRepository.findById(orderId)
                .map(OrderDetailResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#orderId")
    public OrderDetailResponse confirmOrder(Long orderId) {
        return updateStatus(orderId, OrderStatus.CONFIRMED);
    }

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#orderId")
    public OrderDetailResponse startCooking(Long orderId) {
        return updateStatus(orderId, OrderStatus.COOKING);
    }

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#orderId")
    public OrderDetailResponse startDelivering(Long orderId) {
        return updateStatus(orderId, OrderStatus.DELIVERING);
    }

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#orderId")
    public OrderDetailResponse completeOrder(Long orderId) {
        return updateStatus(orderId, OrderStatus.COMPLETED);
    }

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#orderId")
    public OrderDetailResponse cancelOrder(Long orderId) {
        return updateStatus(orderId, OrderStatus.CANCELLED);
    }

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#orderId")
    public OrderDetailResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        return updateStatus(orderId, request.getStatus());
    }

    private OrderDetailResponse updateStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order status for orderId: {} to status: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStateException(
                    String.format("Cannot transition order status from %s to %s", currentStatus, newStatus)
            );
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        log.info("Order status updated successfully for orderId: {}", orderId);

        return OrderDetailResponse.fromEntity(savedOrder);
    }
}
