package com.example.canteendemo.service;

import com.example.canteendemo.entity.Menu;
import com.example.canteendemo.entity.Order;
import com.example.canteendemo.entity.Review;
import com.example.canteendemo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final MenuService menuService;

    @Transactional
    public Review create(Long userId, Long menuId, Long orderId, Integer rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("该订单已经评价过了");
        }

        Order order = orderService.findById(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("只能评价自己的订单");
        }
        if (!"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("只能评价已完成的订单");
        }

        Menu menu = menuService.findById(menuId);
        boolean found = order.getOrderDetails().stream()
                .anyMatch(od -> od.getMenu().getId().equals(menuId));
        if (!found) {
            throw new RuntimeException("该菜品不属于此订单");
        }

        Review review = Review.builder()
                .user(order.getUser())
                .menu(menu)
                .order(order)
                .rating(rating)
                .comment(comment)
                .checkStatus("PENDING")
                .build();
        return reviewRepository.save(review);
    }

    public List<Review> getMenuReviews(Long menuId) {
        return reviewRepository.findByMenuIdOrderByCreateTimeDesc(menuId);
    }

    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public double getMenuAvgRating(Long menuId) {
        List<Review> reviews = reviewRepository.findByMenuIdOrderByCreateTimeDesc(menuId);
        if (reviews.isEmpty()) return 0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }

    @Transactional
    public Review approve(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        review.setCheckStatus("APPROVED");
        return reviewRepository.save(review);
    }

    @Transactional
    public Review reject(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        review.setCheckStatus("REJECTED");
        return reviewRepository.save(review);
    }

    public List<Review> getPendingReviews() {
        return reviewRepository.findByCheckStatus("PENDING");
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreateTimeDesc();
    }
}
