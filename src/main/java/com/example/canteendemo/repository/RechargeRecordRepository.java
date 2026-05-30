package com.example.canteendemo.repository;

import com.example.canteendemo.entity.RechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RechargeRecordRepository extends JpaRepository<RechargeRecord, Long> {

    List<RechargeRecord> findByUserIdOrderByRechargeTimeDesc(Long userId);

    List<RechargeRecord> findAllByOrderByRechargeTimeDesc();
}
