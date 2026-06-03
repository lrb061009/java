package com.example.canteendemo.service;

import com.example.canteendemo.entity.Order;
import com.example.canteendemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

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

        stats.put("totalUsers", userRepository.count());
        stats.put("userCount", userRepository.countByRole("USER"));
        stats.put("merchantCount", userRepository.countByRole("MERCHANT"));
        stats.put("adminCount", userRepository.countByRole("ADMIN"));

        stats.put("pendingOrders", orderRepository.countByStatus("PENDING"));
        stats.put("confirmedOrders", orderRepository.countByStatus("CONFIRMED"));
        stats.put("completedOrders", orderRepository.countByStatus("COMPLETED"));
        stats.put("cancelledOrders", orderRepository.countByStatus("CANCELLED"));
        stats.put("totalRevenue", orderRepository.sumCompletedTotalPrice());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        stats.put("todayOrders", orderRepository.countByOrderTimeBetween(todayStart, todayEnd));

        stats.put("totalMenus", menuRepository.count());
        stats.put("availableMenus", menuRepository.countByStatus("AVAILABLE"));
        stats.put("soldOutMenus", menuRepository.countByStatus("SOLD_OUT"));

        stats.put("paidPayments", paymentRepository.countByPaymentStatus("PAID"));
        stats.put("unpaidPayments", paymentRepository.countByPaymentStatus("UNPAID"));

        stats.put("pendingReviews", reviewRepository.countByCheckStatus("PENDING"));
        stats.put("approvedReviews", reviewRepository.countByCheckStatus("APPROVED"));
        stats.put("rejectedReviews", reviewRepository.countByCheckStatus("REJECTED"));

        return stats;
    }

    public Map<String, Object> getMerchantStats(Long canteenId) {
        return getMerchantStatsByPeriod(canteenId, "all");
    }

    public Map<String, Object> getMerchantStatsByPeriod(Long canteenId, String period) {
        LocalDateTime start;
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        LocalDate today = LocalDate.now();

        switch (period != null ? period : "all") {
            case "today":
                start = today.atStartOfDay();
                break;
            case "month":
                start = today.withDayOfMonth(1).atStartOfDay();
                break;
            case "year":
                start = today.withDayOfYear(1).atStartOfDay();
                break;
            default:
                start = null;
                break;
        }

        Map<String, Object> stats = new LinkedHashMap<>();

        long totalOrders, pending, confirmed, completed, cancelled;
        BigDecimal totalRevenue;

        if (start != null) {
            totalOrders = orderRepository.countByCanteenIdAndOrderTimeBetween(canteenId, start, end);
            pending = orderRepository.countByCanteenIdAndStatusAndOrderTimeBetween(canteenId, "PENDING", start, end);
            confirmed = orderRepository.countByCanteenIdAndStatusAndOrderTimeBetween(canteenId, "CONFIRMED", start, end);
            completed = orderRepository.countByCanteenIdAndStatusAndOrderTimeBetween(canteenId, "COMPLETED", start, end);
            cancelled = orderRepository.countByCanteenIdAndStatusAndOrderTimeBetween(canteenId, "CANCELLED", start, end);
            totalRevenue = orderRepository.sumCompletedTotalPriceByCanteenIdAndOrderTimeBetween(canteenId, start, end);
        } else {
            totalOrders = orderRepository.countByCanteenId(canteenId);
            pending = orderRepository.countByCanteenIdAndStatus(canteenId, "PENDING");
            confirmed = orderRepository.countByCanteenIdAndStatus(canteenId, "CONFIRMED");
            completed = orderRepository.countByCanteenIdAndStatus(canteenId, "COMPLETED");
            cancelled = orderRepository.countByCanteenIdAndStatus(canteenId, "CANCELLED");
            totalRevenue = orderRepository.sumCompletedTotalPriceByCanteenId(canteenId);
        }

        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pending);
        stats.put("confirmedOrders", confirmed);
        stats.put("completedOrders", completed);
        stats.put("cancelledOrders", cancelled);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalMenus", menuRepository.findByCanteenIdOrderByDateDesc(canteenId).size());
        stats.put("reviewCount", reviewRepository.findByMenuCanteenIdOrderByCreateTimeDesc(canteenId).size());

        // Order status distribution for pie chart
        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        statusDistribution.add(statusItem("待处理", pending, "pending"));
        statusDistribution.add(statusItem("已确认", confirmed, "confirmed"));
        statusDistribution.add(statusItem("已完成", completed, "completed"));
        statusDistribution.add(statusItem("已取消", cancelled, "cancelled"));
        stats.put("statusDistribution", statusDistribution);

        // Revenue breakdown for bar chart
        stats.put("revenueBreakdown", buildRevenueBreakdown(canteenId, period, start, end, today));

        return stats;
    }

    private Map<String, Object> statusItem(String name, long count, String type) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("count", count);
        item.put("type", type);
        return item;
    }

    private List<Map<String, Object>> buildRevenueBreakdown(Long canteenId, String period, LocalDateTime start, LocalDateTime end, LocalDate today) {
        List<Map<String, Object>> breakdown = new ArrayList<>();

        if (start == null) {
            // "all" - yearly breakdown
            return buildYearlyBreakdown(canteenId);
        }

        // Fetch completed orders in range and group by day
        List<Order> orders = orderRepository.findCompletedByCanteenIdAndOrderTimeBetween(canteenId, start, end);
        Map<LocalDate, BigDecimal> dailySum = new TreeMap<>();
        for (Order o : orders) {
            LocalDate d = o.getOrderTime().toLocalDate();
            dailySum.merge(d, o.getTotalPrice(), BigDecimal::add);
        }

        switch (period) {
            case "today":
                BigDecimal todayRevenue = dailySum.getOrDefault(today, BigDecimal.ZERO);
                breakdown.add(breakdownItem("今日", todayRevenue));
                break;
            case "month": {
                YearMonth ym = YearMonth.from(today);
                for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                    LocalDate date = ym.atDay(d);
                    BigDecimal val = dailySum.getOrDefault(date, BigDecimal.ZERO);
                    breakdown.add(breakdownItem(String.valueOf(d), val));
                }
                break;
            }
            case "year": {
                for (int m = 1; m <= 12; m++) {
                    BigDecimal monthSum = BigDecimal.ZERO;
                    for (Map.Entry<LocalDate, BigDecimal> e : dailySum.entrySet()) {
                        if (e.getKey().getMonthValue() == m) {
                            monthSum = monthSum.add(e.getValue());
                        }
                    }
                    breakdown.add(breakdownItem(m + "月", monthSum));
                }
                break;
            }
            default:
                break;
        }

        return breakdown;
    }

    private List<Map<String, Object>> buildYearlyBreakdown(Long canteenId) {
        List<Map<String, Object>> breakdown = new ArrayList<>();
        List<Order> orders = orderRepository.findCompletedByCanteenIdAndOrderTimeBetween(
                canteenId,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDate.now().atTime(LocalTime.MAX)
        );

        Map<Integer, BigDecimal> yearSum = new TreeMap<>();
        for (Order o : orders) {
            int y = o.getOrderTime().getYear();
            yearSum.merge(y, o.getTotalPrice(), BigDecimal::add);
        }

        if (yearSum.isEmpty()) {
            yearSum.put(LocalDate.now().getYear(), BigDecimal.ZERO);
        }

        for (Map.Entry<Integer, BigDecimal> e : yearSum.entrySet()) {
            breakdown.add(breakdownItem(e.getKey() + "年", e.getValue()));
        }
        return breakdown;
    }

    private Map<String, Object> breakdownItem(String label, BigDecimal value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }
}
