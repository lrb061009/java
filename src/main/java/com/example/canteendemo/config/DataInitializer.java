package com.example.canteendemo.config;

import com.example.canteendemo.entity.Canteen;
import com.example.canteendemo.entity.Menu;
import com.example.canteendemo.entity.User;
import com.example.canteendemo.repository.CanteenRepository;
import com.example.canteendemo.repository.MenuRepository;
import com.example.canteendemo.repository.OrderRepository;
import com.example.canteendemo.repository.PaymentRepository;
import com.example.canteendemo.repository.ReviewRepository;
import com.example.canteendemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final CanteenRepository canteenRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public void run(String... args) {
        reviewRepository.deleteAll();
        paymentRepository.deleteAll();
        orderRepository.deleteAll();

        List<User> allUsers = userRepository.findAll();
        boolean needsUserSeed = allUsers.isEmpty();

        if (!needsUserSeed) {
            for (User u : allUsers) {
                if (u.getBalance() == null || u.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                    u.setBalance(new BigDecimal("100.00"));
                    userRepository.save(u);
                }
            }
        } else {
            userRepository.save(User.builder()
                    .username("admin").password("123456").realName("管理员").role("ADMIN")
                    .phone("13800000000").department("行政部").balance(new BigDecimal("500.00")).build());
            userRepository.save(User.builder()
                    .username("zhangsan").password("123456").realName("张三").role("USER")
                    .phone("13800000001").department("研发部").balance(new BigDecimal("200.00")).build());
            userRepository.save(User.builder()
                    .username("lisi").password("123456").realName("李四").role("USER")
                    .phone("13800000002").department("市场部").balance(new BigDecimal("150.00")).build());
            userRepository.save(User.builder()
                    .username("merchant1").password("123456").realName("王老板").role("MERCHANT")
                    .phone("13800000003").department("食堂管理").balance(BigDecimal.ZERO)
                    .managedCanteenId(1L).build());
        }

        // Ensure canteens exist
        List<Canteen> canteens = canteenRepository.findAll();
        if (canteens.isEmpty()) {
            canteens = List.of(
                    canteenRepository.save(Canteen.builder().name("第一食堂").build()),
                    canteenRepository.save(Canteen.builder().name("第二食堂").build()),
                    canteenRepository.save(Canteen.builder().name("教工食堂").build())
            );
        }

        // Always ensure today's menu exists
        LocalDate today = LocalDate.now();
        List<Menu> todayMenus = menuRepository.findByDate(today);
        if (!todayMenus.isEmpty()) {
            return;
        }

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
}
