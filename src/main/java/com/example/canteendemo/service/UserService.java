package com.example.canteendemo.service;

import com.example.canteendemo.entity.Canteen;
import com.example.canteendemo.entity.RechargeRecord;
import com.example.canteendemo.entity.User;
import com.example.canteendemo.repository.OrderRepository;
import com.example.canteendemo.repository.RechargeRecordRepository;
import com.example.canteendemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CanteenService canteenService;
    private final RechargeRecordRepository rechargeRecordRepository;

    private static final Set<String> VALID_PAYMENT_METHODS = Set.of("WECHAT", "ALIPAY", "BANK_CARD");

    public User register(String username, String password, String realName, String phone, String department, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        User user = User.builder()
                .username(username)
                .password(password)
                .realName(realName)
                .phone(phone)
                .department(department != null ? department : role)
                .role(role)
                .balance(BigDecimal.ZERO)
                .build();
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User updateProfile(Long id, String realName, String phone, String department) {
        User user = findById(id);
        if (realName != null && !realName.isBlank()) {
            user.setRealName(realName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (department != null) {
            user.setDepartment(department);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User charge(Long userId, BigDecimal amount, String paymentMethod) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        if (paymentMethod == null || !VALID_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new RuntimeException("无效的支付方式");
        }
        User user = findById(userId);
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        rechargeRecordRepository.save(RechargeRecord.builder()
                .user(user)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .build());
        return user;
    }

    @Transactional
    public void deductBalance(Long userId, BigDecimal amount) {
        User user = findById(userId);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足，当前余额：" + user.getBalance() + "，需要：" + amount);
        }
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
    }

    @Transactional
    public void refundBalance(Long userId, BigDecimal amount) {
        User user = findById(userId);
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User adminUpdateUser(Long id, String realName, String role, String phone, String department, Long managedCanteenId) {
        User user = findById(id);
        if (realName != null && !realName.isBlank()) user.setRealName(realName);
        if (role != null && !role.isBlank()) user.setRole(role);
        if (phone != null) user.setPhone(phone);
        if (department != null) user.setDepartment(department);
        if (managedCanteenId != null) user.setManagedCanteenId(managedCanteenId);
        return userRepository.save(user);
    }

    @Transactional
    public User adminCreateUser(String username, String password, String realName, String role, String phone, String department, Long managedCanteenId) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        User user = User.builder()
                .username(username)
                .password(password)
                .realName(realName)
                .role(role)
                .phone(phone)
                .department(department != null ? department : role)
                .managedCanteenId(managedCanteenId)
                .balance(BigDecimal.ZERO)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    @Transactional
    public User merchantWithdraw(Long userId, Long canteenId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("提现金额必须大于0");
        }
        Canteen canteen = canteenService.findById(canteenId);
        BigDecimal totalRevenue = orderRepository.sumCompletedTotalPriceByCanteenId(canteenId);
        BigDecimal alreadyWithdrawn = canteen.getTotalWithdrawn();
        BigDecimal available = totalRevenue.subtract(alreadyWithdrawn);
        if (amount.compareTo(available) > 0) {
            throw new RuntimeException("可提现金额不足，当前可提现：¥" + available);
        }
        User user = findById(userId);
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
        canteenService.recordWithdrawal(canteenId, amount);
        return user;
    }
}
