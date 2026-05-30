package com.example.canteendemo.repository;

import com.example.canteendemo.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByDateAndStatus(LocalDate date, String status);

    List<Menu> findByDateAndMealTypeAndStatus(LocalDate date, String mealType, String status);

    List<Menu> findByDate(LocalDate date);

    List<Menu> findByCanteenIdAndDateAndStatus(Long canteenId, LocalDate date, String status);

    List<Menu> findByCanteenIdAndDateAndMealTypeAndStatus(Long canteenId, LocalDate date, String mealType, String status);

    // Persistent listing — filter by status only (no date)
    List<Menu> findByStatus(String status);

    List<Menu> findByMealTypeAndStatus(String mealType, String status);

    List<Menu> findByCanteenIdAndStatus(Long canteenId, String status);

    List<Menu> findByCanteenIdAndMealTypeAndStatus(Long canteenId, String mealType, String status);

    List<Menu> findByCanteenIdOrderByDateDesc(Long canteenId);

    List<Menu> findAllByOrderByDateDesc();

    long countByStatus(String status);
}