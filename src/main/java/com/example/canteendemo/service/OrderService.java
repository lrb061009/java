package com.example.canteendemo.service;

import com.example.canteendemo.entity.Menu;
import com.example.canteendemo.entity.Order;
import com.example.canteendemo.entity.OrderDetail;
import com.example.canteendemo.entity.Payment;
import com.example.canteendemo.entity.User;
import com.example.canteendemo.repository.OrderRepository;
import com.example.canteendemo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final MenuService menuService;
    private final PaymentRepository paymentRepository;

    public record OrderItemRequest(Long menuId, Integer quantity) {}

    @Transactional
    public Order create(Long userId, List<OrderItemRequest> items, String remark) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("订单必须包含至少一个菜品");
        }

        User user = userService.findById(userId);
        BigDecimal totalPrice = BigDecimal.ZERO;

        // First pass: validate and compute total
        List<Menu> menus = new ArrayList<>();
        for (OrderItemRequest item : items) {
            Menu menu = menuService.findById(item.menuId());
            if (!"AVAILABLE".equals(menu.getStatus())) {
                throw new RuntimeException("菜品[" + menu.getName() + "]已下架或售罄");
            }
            if (menu.getStock() < item.quantity()) {
                throw new RuntimeException("菜品[" + menu.getName() + "]库存不足，当前库存：" + menu.getStock());
            }
            totalPrice = totalPrice.add(menu.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
            menus.add(menu);
        }

        // Check and deduct balance
        userService.deductBalance(userId, totalPrice);

        // Build order
        Order order = Order.builder()
                .user(user)
                .status("PENDING")
                .remark(remark)
                .takeCode(generateTakeCode())
                .totalPrice(totalPrice)
                .orderDetails(new ArrayList<>())
                .build();

        // Second pass: deduct stock and build order details
        for (int i = 0; i < items.size(); i++) {
            OrderItemRequest item = items.get(i);
            Menu menu = menus.get(i);
            menuService.updateStock(item.menuId(), -item.quantity());

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .menu(menu)
                    .quantity(item.quantity())
                    .price(menu.getPrice())
                    .build();
            order.getOrderDetails().add(detail);
        }

        Order savedOrder = orderRepository.save(order);

        paymentRepository.save(Payment.builder()
                .order(savedOrder)
                .paymentMethod("BALANCE")
                .paymentStatus("PAID")
                .build());

        return savedOrder;
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderTimeDesc(userId);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus("PENDING");
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderTimeDesc();
    }

    public List<Order> getCanteenOrders(Long canteenId) {
        return orderRepository.findByCanteenIdOrderByOrderTimeDesc(canteenId);
    }

    @Transactional
    public Order verifyTakeCode(String takeCode, Long canteenId) {
        List<Order> orders = orderRepository.findByCanteenIdOrderByOrderTimeDesc(canteenId);
        for (Order order : orders) {
            if (takeCode.equalsIgnoreCase(order.getTakeCode())) {
                if ("PENDING".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus())) {
                    order.setStatus("COMPLETED");
                    return orderRepository.save(order);
                } else if ("COMPLETED".equals(order.getStatus())) {
                    throw new RuntimeException("该取餐码已核销，请勿重复核销");
                } else {
                    throw new RuntimeException("订单状态[" + order.getStatus() + "]不支持核销");
                }
            }
        }
        throw new RuntimeException("未找到该取餐码对应的订单，请检查取餐码是否正确");
    }

    @Transactional
    public Order confirm(Long orderId) {
        Order order = findById(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只能确认待处理状态的订单");
        }
        order.setStatus("CONFIRMED");
        return orderRepository.save(order);
    }

    @Transactional
    public Order complete(Long orderId) {
        Order order = findById(orderId);
        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new RuntimeException("只能完成已确认状态的订单");
        }
        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancel(Long orderId) {
        Order order = findById(orderId);
        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("已完成的订单无法取消");
        }
        if (!"CANCELLED".equals(order.getStatus())) {
            for (OrderDetail detail : order.getOrderDetails()) {
                menuService.updateStock(detail.getMenu().getId(), detail.getQuantity());
            }
            userService.refundBalance(order.getUser().getId(), order.getTotalPrice());
            paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
                payment.setPaymentStatus("REFUNDED");
                paymentRepository.save(payment);
            });
        }
        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    private String generateTakeCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
