package com.example.canteendemo.controller;

import com.example.canteendemo.entity.Order;
import com.example.canteendemo.entity.User;
import com.example.canteendemo.repository.PaymentRepository;
import com.example.canteendemo.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserService userService;
    private final MenuService menuService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final CanteenService canteenService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final StatisticsService statisticsService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("user", user);
            return "redirect:/home";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String realName,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String department,
                           Model model) {
        try {
            userService.register(username, password, realName, phone, department);
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/home")
    public String home(@RequestParam(required = false) Long canteenId,
                       HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        // Refresh from DB to get latest balance
        User user = userService.findById(sessionUser.getId());
        session.setAttribute("user", user);
        model.addAttribute("user", user);
        model.addAttribute("canteenId", canteenId);
        model.addAttribute("canteens", canteenService.findAll());
        model.addAttribute("todayMenus", menuService.getTodayMenu(canteenId));
        model.addAttribute("today", LocalDate.now());
        return "home";
    }

    @GetMapping("/menu/detail/{id}")
    public String menuDetail(@PathVariable Long id, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        User user = userService.findById(sessionUser.getId());
        session.setAttribute("user", user);
        model.addAttribute("user", user);
        model.addAttribute("menu", menuService.findById(id));
        model.addAttribute("reviews", reviewService.getMenuReviews(id));
        model.addAttribute("avgRating", Math.round(reviewService.getMenuAvgRating(id) * 10.0) / 10.0);
        return "menu-detail";
    }

    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        User user = userService.findById(sessionUser.getId());
        session.setAttribute("user", user);
        List<Order> orders = orderService.getUserOrders(user.getId());
        Map<Long, String> paymentStatusMap = new java.util.HashMap<>();
        for (Order order : orders) {
            paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                paymentStatusMap.put(order.getId(), payment.getPaymentStatus());
            });
        }
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("paymentStatusMap", paymentStatusMap);
        return "my-orders";
    }

    @GetMapping("/my-reviews")
    public String myReviews(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        User user = userService.findById(sessionUser.getId());
        session.setAttribute("user", user);
        model.addAttribute("user", user);
        model.addAttribute("reviews", reviewService.getUserReviews(user.getId()));
        return "my-reviews";
    }

    @PostMapping("/charge")
    public String charge(@RequestParam Long userId,
                         @RequestParam BigDecimal amount,
                         HttpSession session) {
        userService.charge(userId, amount);
        User user = userService.findById(userId);
        session.setAttribute("user", user);
        return "redirect:/home";
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !"ADMIN".equals(sessionUser.getRole())) {
            return "redirect:/login";
        }
        User user = userService.findById(sessionUser.getId());
        session.setAttribute("user", user);
        List<Order> allOrders = orderService.getAllOrders();
        Map<Long, String> paymentStatusMap = new java.util.HashMap<>();
        for (Order order : allOrders) {
            paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                paymentStatusMap.put(order.getId(), payment.getPaymentStatus());
            });
        }
        model.addAttribute("user", user);
        model.addAttribute("stats", statisticsService.getDashboardStats());
        model.addAttribute("orders", allOrders);
        model.addAttribute("paymentStatusMap", paymentStatusMap);
        model.addAttribute("payments", paymentService.getAllPayments());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("reviews", reviewService.getAllReviews());
        model.addAttribute("pendingReviews", reviewService.getPendingReviews());
        model.addAttribute("canteens", canteenService.findAll());
        return "admin";
    }

    @GetMapping("/merchant")
    public String merchantDashboard(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null || !"MERCHANT".equals(sessionUser.getRole())) {
            return "redirect:/login";
        }
        User user = userService.findById(sessionUser.getId());
        session.setAttribute("user", user);
        model.addAttribute("user", user);
        Long canteenId = user.getManagedCanteenId();
        if (canteenId != null) {
            model.addAttribute("canteenMenus", menuService.getCanteenMenus(canteenId));
            List<Order> canteenOrders = orderService.getCanteenOrders(canteenId);
            Map<Long, String> paymentStatusMap = new java.util.HashMap<>();
            for (Order order : canteenOrders) {
                paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                    paymentStatusMap.put(order.getId(), payment.getPaymentStatus());
                });
            }
            model.addAttribute("canteenOrders", canteenOrders);
            model.addAttribute("paymentStatusMap", paymentStatusMap);
            model.addAttribute("myCanteen", canteenService.findById(canteenId));
            model.addAttribute("today", LocalDate.now());
        }
        model.addAttribute("canteens", canteenService.findAll());
        return "merchant";
    }
}
