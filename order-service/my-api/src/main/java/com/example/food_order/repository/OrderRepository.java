package com.example.food_order.repository;

import com.example.food_order.entity.Order;
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

    // Custom query for sorting all orders with 'CANCELLED' and 'COMPLETED' at the end
    @Query("SELECT o FROM Order o WHERE o.slipId LIKE %:searchId% ORDER BY " +
            "CASE " +
            "  WHEN o.status = 'CANCELLED' THEN 2 " +
            "  WHEN o.status = 'COMPLETED' THEN 1 " +
            "  ELSE 0 " +
            "END ASC")
    Page<Order> findByIdContainingOrderByStatusCustom(String searchId, Pageable pageable);

    @Query("SELECT o FROM Order o ORDER BY " +
            "CASE " +
            "  WHEN o.status = 'CANCELLED' THEN 2 " +
            "  WHEN o.status = 'COMPLETED' THEN 1 " +
            "  ELSE 0 " +
            "END ASC")
    Page<Order> findAllByOrderByStatusAscCreatedAtDesc(Pageable pageable);
}
