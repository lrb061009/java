package com.example.canteendemo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recharge_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "balance"})
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String paymentMethod;

    @Column(nullable = false, updatable = false)
    private LocalDateTime rechargeTime;

    @PrePersist
    protected void onCreate() {
        this.rechargeTime = LocalDateTime.now();
    }
}
