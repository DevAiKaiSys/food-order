package com.example.food_order.repository;

import com.example.food_order.entity.Order;
import com.example.food_order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Long countOrdersByDate(@Param("startDate") ZonedDateTime startDate, @Param("endDate") ZonedDateTime endDate);

    // ค้นหาทั้งหมด - เรียง CANCELLED และ COMPLETED ไว้ท้ายสุด, อื่นๆ
    @Query("SELECT o FROM Order o ORDER BY " +
            "CASE " +
            "  WHEN o.status = 'CANCELLED' THEN 2 " +
            "  WHEN o.status = 'COMPLETED' THEN 1 " +
            "  ELSE 0 " +
            "END ASC, o.createdAt ASC")
    Page<Order> findAllByOrderByStatusAscCreatedAtDesc(Pageable pageable);

    // ค้นหาด้วย slipId - เรียง status แบบ custom
    @Query("SELECT o FROM Order o WHERE o.slipId LIKE %:searchId% ORDER BY " +
            "CASE " +
            "  WHEN o.status = 'CANCELLED' THEN 2 " +
            "  WHEN o.status = 'COMPLETED' THEN 1 " +
            "  ELSE 0 " +
            "END ASC, o.createdAt ASC")
    Page<Order> findBySlipIdContainingOrderByStatusCustom(Pageable pageable, @Param("searchId") String searchId);

    // ค้นหาด้วย status เฉพาะ - เรียงตาม createdAt
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY " +
            "CASE " +
            "  WHEN o.status = 'CANCELLED' THEN 2 " +
            "  WHEN o.status = 'COMPLETED' THEN 1 " +
            "  ELSE 0 " +
            "END ASC, o.createdAt ASC")
    Page<Order> findByStatusOrderByStatusCustom(Pageable pageable, @Param("status") OrderStatus status);

    // ค้นหาด้วยทั้ง status และ slipId
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.slipId LIKE %:searchId% ORDER BY " +
            "CASE " +
            "  WHEN o.status = 'CANCELLED' THEN 2 " +
            "  WHEN o.status = 'COMPLETED' THEN 1 " +
            "  ELSE 0 " +
            "END ASC, o.createdAt ASC")
    Page<Order> findByStatusAndSlipIdContainingOrderByStatusCustom(
            Pageable pageable,
            @Param("status") OrderStatus status,
            @Param("searchId") String searchId);
}
