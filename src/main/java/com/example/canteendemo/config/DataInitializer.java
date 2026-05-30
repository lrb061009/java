package com.example.canteendemo.config;

import com.example.canteendemo.entity.Canteen;
import com.example.canteendemo.entity.Menu;
import com.example.canteendemo.entity.User;
import com.example.canteendemo.repository.CanteenRepository;
import com.example.canteendemo.repository.MenuRepository;
import com.example.canteendemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final CanteenRepository canteenRepository;

    @Override
    public void run(String... args) {
        // Records are preserved permanently — no daily clearing

        // Ensure seed users always exist
        ensureUser("admin", "123456", "管理员", "ADMIN", "13800000000", "行政部", new BigDecimal("500.00"), null);
        ensureUser("zhangsan", "123456", "张三", "USER", "13800000001", "研发部", new BigDecimal("200.00"), null);
        ensureUser("lisi", "123456", "李四", "USER", "13800000002", "市场部", new BigDecimal("150.00"), null);
        ensureUser("merchant1", "123456", "王老板", "MERCHANT", "13800000003", "食堂管理", BigDecimal.ZERO, 1L);

        // Ensure canteens exist
        List<Canteen> canteens = canteenRepository.findAll();
        if (canteens.isEmpty()) {
            canteens = List.of(
                    canteenRepository.save(Canteen.builder().name("第一食堂").build()),
                    canteenRepository.save(Canteen.builder().name("第二食堂").build()),
                    canteenRepository.save(Canteen.builder().name("教工食堂").build())
            );
        }

        // Deduplicate menus from old daily-seed system (same name + canteen + mealType)
        List<Menu> allMenus = menuRepository.findAll();
        Map<String, List<Menu>> groups = new HashMap<>();
        for (Menu m : allMenus) {
            String key = m.getName() + "|" + (m.getCanteen() != null ? m.getCanteen().getId() : "0") + "|" + m.getMealType();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }
        for (List<Menu> group : groups.values()) {
            if (group.size() > 1) {
                // Keep the newest one, delete the rest
                group.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                for (int i = 1; i < group.size(); i++) {
                    menuRepository.delete(group.get(i));
                }
            }
        }

        // Only seed menus if none exist yet (menus persist across days)
        if (menuRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        Canteen c1 = canteens.get(0);
        Canteen c2 = canteens.get(1);
        Canteen c3 = canteens.get(2);

        menuRepository.save(Menu.builder()
                .date(today).mealType("BREAKFAST").name("豆浆油条套餐")
                .price(new BigDecimal("6.00")).description("现磨豆浆配酥脆油条，经典早餐搭配")
                .stock(50).status("AVAILABLE").canteen(c1).build());
        menuRepository.save(Menu.builder()
                .date(today).mealType("BREAKFAST").name("鸡蛋灌饼")
                .price(new BigDecimal("8.00")).description("现做鸡蛋灌饼，加生菜、火腿肠")
                .stock(30).status("AVAILABLE").canteen(c1).build());
        menuRepository.save(Menu.builder()
                .date(today).mealType("BREAKFAST").name("小米粥套餐")
                .price(new BigDecimal("5.00")).description("小米粥配咸菜、煮鸡蛋")
                .stock(40).status("AVAILABLE").canteen(c2).build());

        menuRepository.save(Menu.builder()
                .date(today).mealType("LUNCH").name("红烧肉套餐")
                .price(new BigDecimal("18.00")).description("红烧肉、青菜、米饭，荤素搭配营养均衡")
                .stock(100).status("AVAILABLE").canteen(c1).build());
        menuRepository.save(Menu.builder()
                .date(today).mealType("LUNCH").name("宫保鸡丁套餐")
                .price(new BigDecimal("16.00")).description("宫保鸡丁配时蔬，麻辣鲜香")
                .stock(80).status("AVAILABLE").canteen(c2).build());
        menuRepository.save(Menu.builder()
                .date(today).mealType("LUNCH").name("素菜套餐")
                .price(new BigDecimal("12.00")).description("清炒时蔬、香菇豆腐、米饭")
                .stock(60).status("AVAILABLE").canteen(c3).build());
        menuRepository.save(Menu.builder()
                .date(today).mealType("LUNCH").name("牛肉面")
                .price(new BigDecimal("22.00")).description("手工拉面配红烧牛肉，汤浓面劲道")
                .stock(50).status("AVAILABLE").canteen(c2).build());

        menuRepository.save(Menu.builder()
                .date(today).mealType("DINNER").name("麻辣香锅")
                .price(new BigDecimal("25.00")).description("自选食材麻辣香锅，配米饭")
                .stock(70).status("AVAILABLE").canteen(c1).build());
        menuRepository.save(Menu.builder()
                .date(today).mealType("DINNER").name("水饺")
                .price(new BigDecimal("15.00")).description("猪肉白菜馅手工水饺，配醋和辣椒油")
                .stock(80).status("AVAILABLE").canteen(c2).build());
        menuRepository.save(Menu.builder()
                .date(today).mealType("DINNER").name("番茄蛋汤面")
                .price(new BigDecimal("10.00")).description("番茄鸡蛋汤配手工面，清淡可口")
                .stock(60).status("AVAILABLE").canteen(c3).build());
    }

    private void ensureUser(String username, String password, String realName, String role,
                            String phone, String department, BigDecimal balance, Long managedCanteenId) {
        if (!userRepository.existsByUsername(username)) {
            User.UserBuilder builder = User.builder()
                    .username(username).password(password).realName(realName).role(role)
                    .phone(phone).department(department).balance(balance);
            if (managedCanteenId != null) {
                builder.managedCanteenId(managedCanteenId);
            }
            userRepository.save(builder.build());
        }
    }
}
