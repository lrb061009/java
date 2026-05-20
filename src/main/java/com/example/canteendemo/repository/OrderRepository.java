package com.example.canteendemo.repository;

import com.example.canteendemo.entity.Order;
import com.example.canteendemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderTimeDesc(User user);

    List<Order> findByUserIdOrderByOrderTimeDesc(Long userId);

    List<Order> findByStatus(String status);

    List<Order> findByUserAndStatus(User user, String status);

    List<Order> findAllByOrderByOrderTimeDesc();

    @Query("SELECT o FROM Order o JOIN o.orderDetails od WHERE od.menu.canteen.id = :canteenId ORDER BY o.orderTime DESC")
    List<Order> findByCanteenIdOrderByOrderTimeDesc(@Param("canteenId") Long canteenId);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    java.math.BigDecimal sumCompletedTotalPrice();

    long countByOrderTimeBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}