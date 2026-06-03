package com.example.canteendemo.repository;

import com.example.canteendemo.entity.Order;
import com.example.canteendemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderTimeDesc(User user);

    List<Order> findByUserIdOrderByOrderTimeDesc(Long userId);

    List<Order> findByStatus(String status);

    List<Order> findByUserAndStatus(User user, String status);

    List<Order> findAllByOrderByOrderTimeDesc();

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderDetails od WHERE od.menu.canteen.id = :canteenId ORDER BY o.orderTime DESC")
    List<Order> findByCanteenIdOrderByOrderTimeDesc(@Param("canteenId") Long canteenId);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    java.math.BigDecimal sumCompletedTotalPrice();

    long countByOrderTimeBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'COMPLETED' AND EXISTS (SELECT 1 FROM OrderDetail od WHERE od.order = o AND od.menu.canteen.id = :canteenId)")
    java.math.BigDecimal sumCompletedTotalPriceByCanteenId(@Param("canteenId") Long canteenId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderDetails od WHERE od.menu.canteen.id = :canteenId")
    long countByCanteenId(@Param("canteenId") Long canteenId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderDetails od WHERE od.menu.canteen.id = :canteenId AND o.status = :status")
    long countByCanteenIdAndStatus(@Param("canteenId") Long canteenId, @Param("status") String status);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderDetails od WHERE od.menu.canteen.id = :canteenId AND o.orderTime BETWEEN :start AND :end")
    long countByCanteenIdAndOrderTimeBetween(@Param("canteenId") Long canteenId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderDetails od WHERE od.menu.canteen.id = :canteenId AND o.status = :status AND o.orderTime BETWEEN :start AND :end")
    long countByCanteenIdAndStatusAndOrderTimeBetween(@Param("canteenId") Long canteenId, @Param("status") String status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'COMPLETED' AND EXISTS (SELECT 1 FROM OrderDetail od WHERE od.order = o AND od.menu.canteen.id = :canteenId) AND o.orderTime BETWEEN :start AND :end")
    java.math.BigDecimal sumCompletedTotalPriceByCanteenIdAndOrderTimeBetween(@Param("canteenId") Long canteenId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderDetails od WHERE od.menu.canteen.id = :canteenId AND o.status = 'COMPLETED' AND o.orderTime BETWEEN :start AND :end ORDER BY o.orderTime")
    List<Order> findCompletedByCanteenIdAndOrderTimeBetween(@Param("canteenId") Long canteenId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}