package com.example.canteendemo.service;

import com.example.canteendemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final ReviewRepository reviewRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // User stats
        stats.put("totalUsers", userRepository.count());
        stats.put("userCount", userRepository.countByRole("USER"));
        stats.put("merchantCount", userRepository.countByRole("MERCHANT"));
        stats.put("adminCount", userRepository.countByRole("ADMIN"));

        // Order stats
        stats.put("pendingOrders", orderRepository.countByStatus("PENDING"));
        stats.put("confirmedOrders", orderRepository.countByStatus("CONFIRMED"));
        stats.put("completedOrders", orderRepository.countByStatus("COMPLETED"));
        stats.put("cancelledOrders", orderRepository.countByStatus("CANCELLED"));
        stats.put("totalRevenue", orderRepository.sumCompletedTotalPrice());

        // Today's orders
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        stats.put("todayOrders", orderRepository.countByOrderTimeBetween(todayStart, todayEnd));

        // Menu stats
        stats.put("totalMenus", menuRepository.count());
        stats.put("availableMenus", menuRepository.countByStatus("AVAILABLE"));
        stats.put("soldOutMenus", menuRepository.countByStatus("SOLD_OUT"));

        // Payment stats
        stats.put("paidPayments", paymentRepository.countByPaymentStatus("PAID"));
        stats.put("unpaidPayments", paymentRepository.countByPaymentStatus("UNPAID"));

        // Review stats
        stats.put("pendingReviews", reviewRepository.countByCheckStatus("PENDING"));
        stats.put("approvedReviews", reviewRepository.countByCheckStatus("APPROVED"));
        stats.put("rejectedReviews", reviewRepository.countByCheckStatus("REJECTED"));

        return stats;
    }
}
