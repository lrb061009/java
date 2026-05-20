package com.example.canteendemo.controller;

import com.example.canteendemo.entity.*;
import com.example.canteendemo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final UserService userService;
    private final MenuService menuService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final CanteenService canteenService;
    private final PaymentService paymentService;
    private final StatisticsService statisticsService;

    // ==================== 用户相关 ====================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            User user = userService.register(
                    body.get("username"),
                    body.get("password"),
                    body.get("realName"),
                    body.get("phone"),
                    body.get("department")
            );
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("role", user.getRole());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            User user = userService.login(
                    body.get("username"),
                    body.get("password")
            );
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("role", user.getRole());
            result.put("department", user.getDepartment());
            result.put("balance", user.getBalance());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("role", user.getRole());
            result.put("phone", user.getPhone());
            result.put("department", user.getDepartment());
            result.put("balance", user.getBalance());
            result.put("createdAt", user.getCreatedAt());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/user/charge")
    public ResponseEntity<?> charge(@RequestBody Map<String, String> body) {
        try {
            Long userId = Long.parseLong(body.get("userId"));
            BigDecimal amount = new BigDecimal(body.get("amount"));
            User user = userService.charge(userId, amount);
            return ResponseEntity.ok(Map.of("message", "充值成功", "balance", user.getBalance()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 食堂相关 ====================

    @GetMapping("/canteens")
    public ResponseEntity<?> getCanteens() {
        return ResponseEntity.ok(canteenService.findAll());
    }

    @PostMapping("/canteen")
    public ResponseEntity<?> createCanteen(@RequestBody Map<String, String> body) {
        try {
            Canteen canteen = canteenService.create(body.get("name"));
            return ResponseEntity.ok(canteen);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/canteen/{id}")
    public ResponseEntity<?> deleteCanteen(@PathVariable Long id) {
        try {
            canteenService.delete(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 菜单相关 ====================

    @GetMapping("/menus/today")
    public ResponseEntity<?> getTodayMenus(
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) Long canteenId) {
        if (mealType != null && !mealType.isBlank()) {
            if (canteenId != null) {
                return ResponseEntity.ok(menuService.getTodayMenuByMealType(mealType, canteenId));
            }
            return ResponseEntity.ok(menuService.getTodayMenuByMealType(mealType));
        }
        return ResponseEntity.ok(menuService.getTodayMenu(canteenId));
    }

    @GetMapping("/menus/date/{date}")
    public ResponseEntity<?> getMenuByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(menuService.getMenuByDate(date));
    }

    @GetMapping("/menu/{id}")
    public ResponseEntity<?> getMenu(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(menuService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/menu")
    public ResponseEntity<?> createMenu(@RequestBody Map<String, Object> body) {
        try {
            Long canteenId = body.get("canteenId") != null
                    ? ((Number) body.get("canteenId")).longValue() : null;
            Menu menu = menuService.create(
                    LocalDate.parse((String) body.get("date")),
                    (String) body.get("mealType"),
                    (String) body.get("name"),
                    new BigDecimal(body.get("price").toString()),
                    (String) body.getOrDefault("description", ""),
                    (String) body.getOrDefault("image", null),
                    body.get("stock") != null ? ((Number) body.get("stock")).intValue() : null,
                    canteenId
            );
            return ResponseEntity.ok(menu);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {
        try {
            menuService.delete(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 订单相关 ====================

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            Long userId = ((Number) body.get("userId")).longValue();
            String remark = (String) body.getOrDefault("remark", null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) body.get("items");
            List<OrderService.OrderItemRequest> items = itemsRaw.stream()
                    .map(m -> new OrderService.OrderItemRequest(
                            ((Number) m.get("menuId")).longValue(),
                            ((Number) m.get("quantity")).intValue()))
                    .toList();

            Order order = orderService.create(userId, items, remark);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/order/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.confirm(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/order/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.complete(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/order/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.cancel(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 支付相关 ====================

    @PostMapping("/payment")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, String> body) {
        try {
            Long orderId = Long.parseLong(body.get("orderId"));
            String method = body.getOrDefault("paymentMethod", "WECHAT");
            Payment payment = paymentService.create(orderId, method);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/payment/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrder(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(paymentService.findByOrderId(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 评价相关 ====================

    @PostMapping("/review")
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> body) {
        try {
            Review review = reviewService.create(
                    ((Number) body.get("userId")).longValue(),
                    ((Number) body.get("menuId")).longValue(),
                    ((Number) body.get("orderId")).longValue(),
                    ((Number) body.get("rating")).intValue(),
                    (String) body.getOrDefault("comment", "")
            );
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reviews/menu/{menuId}")
    public ResponseEntity<?> getMenuReviews(@PathVariable Long menuId) {
        List<Review> reviews = reviewService.getMenuReviews(menuId);
        double avgRating = reviewService.getMenuAvgRating(menuId);
        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "avgRating", Math.round(avgRating * 10.0) / 10.0,
                "count", reviews.size()
        ));
    }

    @PutMapping("/review/{id}/approve")
    public ResponseEntity<?> approveReview(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reviewService.approve(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/review/{id}/reject")
    public ResponseEntity<?> rejectReview(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reviewService.reject(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 商家相关 ====================

    @GetMapping("/merchant/menus")
    public ResponseEntity<?> getMerchantMenus(@RequestParam Long canteenId) {
        return ResponseEntity.ok(menuService.getCanteenMenus(canteenId));
    }

    @PostMapping("/merchant/menu")
    public ResponseEntity<?> merchantCreateMenu(@RequestBody Map<String, Object> body) {
        try {
            Long canteenId = body.get("canteenId") != null
                    ? ((Number) body.get("canteenId")).longValue() : null;
            Menu menu = menuService.create(
                    LocalDate.parse((String) body.get("date")),
                    (String) body.get("mealType"),
                    (String) body.get("name"),
                    new BigDecimal(body.get("price").toString()),
                    (String) body.getOrDefault("description", ""),
                    (String) body.getOrDefault("image", null),
                    body.get("stock") != null ? ((Number) body.get("stock")).intValue() : null,
                    canteenId
            );
            return ResponseEntity.ok(menu);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/merchant/menu/{id}")
    public ResponseEntity<?> merchantUpdateMenu(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long canteenId = body.get("canteenId") != null
                    ? ((Number) body.get("canteenId")).longValue() : null;
            Menu menu = menuService.update(id,
                    body.get("date") != null ? LocalDate.parse((String) body.get("date")) : null,
                    (String) body.get("mealType"),
                    (String) body.get("name"),
                    body.get("price") != null ? new BigDecimal(body.get("price").toString()) : null,
                    (String) body.get("description"),
                    (String) body.get("image"),
                    body.get("stock") != null ? ((Number) body.get("stock")).intValue() : null,
                    (String) body.get("status"),
                    canteenId
            );
            return ResponseEntity.ok(menu);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/merchant/menu/{id}/toggle")
    public ResponseEntity<?> merchantToggleMenu(@PathVariable Long id) {
        try {
            menuService.toggleStatus(id);
            return ResponseEntity.ok(Map.of("message", "操作成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/merchant/orders")
    public ResponseEntity<?> getMerchantOrders(@RequestParam Long canteenId) {
        return ResponseEntity.ok(orderService.getCanteenOrders(canteenId));
    }

    @GetMapping("/merchant/order/{id}")
    public ResponseEntity<?> getMerchantOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/merchant/verify")
    public ResponseEntity<?> verifyTakeCode(@RequestBody Map<String, String> body) {
        try {
            String takeCode = body.get("takeCode");
            Long canteenId = Long.parseLong(body.get("canteenId"));
            Order order = orderService.verifyTakeCode(takeCode, canteenId);
            return ResponseEntity.ok(Map.of("message", "核销成功", "orderId", order.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 管理員相关 ====================

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getAdminStats() {
        return ResponseEntity.ok(statisticsService.getDashboardStats());
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/admin/payments")
    public ResponseEntity<?> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/admin/user/{id}")
    public ResponseEntity<?> adminUpdateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long canteenId = body.get("managedCanteenId") != null
                    ? ((Number) body.get("managedCanteenId")).longValue() : null;
            User user = userService.adminUpdateUser(id,
                    (String) body.get("realName"),
                    (String) body.get("role"),
                    (String) body.get("phone"),
                    (String) body.get("department"),
                    canteenId
            );
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/reviews")
    public ResponseEntity<?> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/admin/reviews/pending")
    public ResponseEntity<?> getPendingReviews() {
        return ResponseEntity.ok(reviewService.getPendingReviews());
    }

    @PutMapping("/order/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if ("CONFIRMED".equals(status)) {
                return ResponseEntity.ok(orderService.confirm(id));
            } else if ("COMPLETED".equals(status)) {
                return ResponseEntity.ok(orderService.complete(id));
            } else if ("CANCELLED".equals(status)) {
                return ResponseEntity.ok(orderService.cancel(id));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "无效的订单状态"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
