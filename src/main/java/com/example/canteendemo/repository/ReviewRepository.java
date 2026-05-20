package com.example.canteendemo.repository;

import com.example.canteendemo.entity.Review;
import com.example.canteendemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMenuIdOrderByCreateTimeDesc(Long menuId);

    List<Review> findByUserIdOrderByCreateTimeDesc(Long userId);

    Optional<Review> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<Review> findByCheckStatus(String checkStatus);

    List<Review> findAllByOrderByCreateTimeDesc();

    long countByCheckStatus(String checkStatus);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.menu.id = :menuId AND r.checkStatus = 'APPROVED'")
    double avgRatingByMenuId(@org.springframework.data.repository.query.Param("menuId") Long menuId);
}