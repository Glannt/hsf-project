package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.constants.enums.OrderStatus;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    List<Order> findByStatus(OrderStatus status);

    // Tổng doanh thu
    @Query("SELECT SUM(o.totalPrice) FROM Order o")
    Double sumTotalRevenue();

    @Query("SELECT o FROM Order o WHERE o.user = :user AND SIZE(o.orderItems) > 0 ORDER BY o.orderDate DESC")
    Page<Order> findByUserWithDetails(User user, Pageable pageable);
}

