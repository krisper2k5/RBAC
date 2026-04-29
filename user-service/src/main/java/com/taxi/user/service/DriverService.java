package com.taxi.user.service;

import com.taxi.common.dto.DriverDto;
import com.taxi.common.enums.DriverStatus;
import com.taxi.user.entity.Driver;
import com.taxi.user.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;
    private final StringRedisTemplate redisTemplate;
    private static final String AVAILABLE_DRIVERS_KEY = "drivers:available";

    @Transactional
    public Driver createDriver(DriverDto dto) {
        Driver driver = new Driver();
        driver.setName(dto.getName());
        driver.setEmail(dto.getEmail());
        driver.setPhone(dto.getPhone());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setStatus(DriverStatus.OFFLINE);
        return driverRepository.save(driver);
    }

    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));
    }

    @Transactional
    public Driver updateDriverStatus(Long id, DriverStatus status) {
        Driver driver = getDriverById(id);
        driver.setStatus(status);
        driverRepository.save(driver);

        // Синхронизация Redis
        if (status == DriverStatus.AVAILABLE) {
            redisTemplate.opsForSet().add(AVAILABLE_DRIVERS_KEY, String.valueOf(id));
        } else {
            redisTemplate.opsForSet().remove(AVAILABLE_DRIVERS_KEY, String.valueOf(id));
        }
        return driver;
    }

    @Transactional
    public Driver findAndReserveAvailableDriver() {
        // Атомарный захват через БД (FOR UPDATE SKIP LOCKED)
        return driverRepository.findFirstAvailableDriver()
                .map(driver -> {
                    driver.setStatus(DriverStatus.BUSY);
                    // Удаляем из Redis, так как водитель теперь занят
                    redisTemplate.opsForSet().remove(AVAILABLE_DRIVERS_KEY, String.valueOf(driver.getId()));
                    return driver;
                })
                .orElseThrow(() -> new RuntimeException("No available drivers"));
    }

    // Метод для инициализации кэша при старте (опционально)
    @Transactional
    public void syncAvailableDriversCache() {
        Set<String> availableIds = driverRepository.findAll().stream()
                .filter(d -> d.getStatus() == DriverStatus.AVAILABLE)
                .map(d -> String.valueOf(d.getId()))
                .collect(java.util.stream.Collectors.toSet());

        redisTemplate.delete(AVAILABLE_DRIVERS_KEY);
        if (!availableIds.isEmpty()) {
            redisTemplate.opsForSet().add(AVAILABLE_DRIVERS_KEY, availableIds.toArray(new String[0]));
        }
    }
}