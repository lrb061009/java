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

    boolean existsByOrderIdAndMenuId(Long orderId, Long menuId);

    List<Review> findByCheckStatus(String checkStatus);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Review r JOIN r.menu m WHERE m.canteen.id = :canteenId ORDER BY r.createTime DESC")
    List<Review> findByMenuCanteenIdOrderByCreateTimeDesc(@org.springframework.data.repository.query.Param("canteenId") Long canteenId);

    List<Review> findAllByOrderByCreateTimeDesc();

    long countByCheckStatus(String checkStatus);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.menu.id = :menuId AND r.checkStatus = 'APPROVED'")
    double avgRatingByMenuId(@org.springframework.data.repository.query.Param("menuId") Long menuId);
}