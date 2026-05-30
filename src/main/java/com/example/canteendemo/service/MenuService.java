package com.example.canteendemo.service;

import com.example.canteendemo.entity.Canteen;
import com.example.canteendemo.entity.Menu;
import com.example.canteendemo.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CanteenService canteenService;

    public Menu create(LocalDate date, String mealType, String name, BigDecimal price,
                       String description, String image, Integer stock, Long canteenId) {
        Canteen canteen = null;
        if (canteenId != null) {
            canteen = canteenService.findById(canteenId);
        }
        Menu menu = Menu.builder()
                .date(date)
                .mealType(mealType)
                .name(name)
                .price(price)
                .description(description)
                .image(image)
                .stock(stock != null ? stock : 0)
                .status("AVAILABLE")
                .canteen(canteen)
                .build();
        return menuRepository.save(menu);
    }

    public Menu update(Long id, LocalDate date, String mealType, String name,
                       BigDecimal price, String description, String image, Integer stock,
                       String status, Long canteenId) {
        Menu menu = findById(id);
        if (date != null) menu.setDate(date);
        if (mealType != null) menu.setMealType(mealType);
        if (name != null) menu.setName(name);
        if (price != null) menu.setPrice(price);
        if (description != null) menu.setDescription(description);
        if (image != null) menu.setImage(image);
        if (stock != null) menu.setStock(stock);
        if (status != null) menu.setStatus(status);
        if (canteenId != null) menu.setCanteen(canteenService.findById(canteenId));
        return menuRepository.save(menu);
    }

    public Menu findById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));
    }

    // Menus persist across days — show all available menus regardless of date
    public List<Menu> getTodayMenu() {
        return menuRepository.findByStatus("AVAILABLE");
    }

    public List<Menu> getTodayMenu(Long canteenId) {
        if (canteenId != null) {
            return menuRepository.findByCanteenIdAndStatus(canteenId, "AVAILABLE");
        }
        return getTodayMenu();
    }

    public List<Menu> getTodayMenuByMealType(String mealType) {
        return menuRepository.findByMealTypeAndStatus(mealType, "AVAILABLE");
    }

    public List<Menu> getTodayMenuByMealType(String mealType, Long canteenId) {
        if (canteenId != null) {
            return menuRepository.findByCanteenIdAndMealTypeAndStatus(canteenId, mealType, "AVAILABLE");
        }
        return getTodayMenuByMealType(mealType);
    }

    public List<Menu> getMenuByDate(LocalDate date) {
        return menuRepository.findByDate(date);
    }

    public void delete(Long id) {
        menuRepository.deleteById(id);
    }

    public void updateStock(Long menuId, int delta) {
        Menu menu = findById(menuId);
        int oldStock = menu.getStock();
        int newStock = oldStock + delta;
        if (newStock < 0) {
            throw new RuntimeException("库存不足");
        }
        menu.setStock(newStock);
        if (newStock == 0) {
            menu.setStatus("SOLD_OUT");
        } else if (oldStock == 0) {
            menu.setStatus("AVAILABLE");
        }
        menuRepository.save(menu);
    }

    public List<Menu> getCanteenMenus(Long canteenId) {
        return menuRepository.findByCanteenIdOrderByDateDesc(canteenId);
    }

    public List<Menu> getAllMenus() {
        return menuRepository.findAllByOrderByDateDesc();
    }

    public void toggleStatus(Long id) {
        Menu menu = findById(id);
        if ("AVAILABLE".equals(menu.getStatus())) {
            menu.setStatus("SOLD_OUT");
        } else {
            menu.setStatus("AVAILABLE");
        }
        menuRepository.save(menu);
    }
}
