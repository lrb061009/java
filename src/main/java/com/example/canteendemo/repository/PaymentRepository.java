package com.example.canteendemo.repository;

import com.example.canteendemo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findAllByOrderByPaymentTimeDesc();

    List<Payment> findByPaymentStatus(String status);

    long countByPaymentStatus(String status);
}
