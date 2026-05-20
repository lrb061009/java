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

    List<Menu> findByCanteenIdOrderByDateDesc(Long canteenId);

    List<Menu> findAllByOrderByDateDesc();

    long countByStatus(String status);
}