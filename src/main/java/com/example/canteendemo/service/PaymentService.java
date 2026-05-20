package com.example.canteendemo.service;

import com.example.canteendemo.entity.Order;
import com.example.canteendemo.entity.Payment;
import com.example.canteendemo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Transactional
    public Payment create(Long orderId, String paymentMethod) {
        Order order = orderService.findById(orderId);
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentStatus("PAID")
                .build();
        return paymentRepository.save(payment);
    }

    public Payment findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
    }

    @Transactional
    public Payment updateStatus(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        payment.setPaymentStatus(status);
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByPaymentTimeDesc();
    }
}
