package com.example.canteendemo.service;

import com.example.canteendemo.entity.Canteen;
import com.example.canteendemo.repository.CanteenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CanteenService {

    private final CanteenRepository canteenRepository;

    public Canteen create(String name) {
        return canteenRepository.save(Canteen.builder().name(name).build());
    }

    public Canteen findById(Long id) {
        return canteenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食堂不存在"));
    }

    public List<Canteen> findAll() {
        return canteenRepository.findAll();
    }

    public Canteen update(Long id, String name) {
        Canteen canteen = findById(id);
        canteen.setName(name);
        return canteenRepository.save(canteen);
    }

    public void delete(Long id) {
        canteenRepository.deleteById(id);
    }

    @Transactional
    public void recordWithdrawal(Long canteenId, BigDecimal amount) {
        Canteen canteen = findById(canteenId);
        canteen.setTotalWithdrawn(canteen.getTotalWithdrawn().add(amount));
        canteenRepository.save(canteen);
    }
}
