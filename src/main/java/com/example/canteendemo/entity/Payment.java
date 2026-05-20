package com.example.canteendemo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"orderDetails", "user"})
    private Order order;

    @Column(length = 20)
    private String paymentMethod;

    @Column(nullable = false, updatable = false)
    private LocalDateTime paymentTime;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String paymentStatus = "UNPAID";

    @PrePersist
    protected void onCreate() {
        this.paymentTime = LocalDateTime.now();
    }
}
