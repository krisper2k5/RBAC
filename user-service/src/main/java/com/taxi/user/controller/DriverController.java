package com.taxi.user.controller;
import com.taxi.common.dto.DriverDto;
import com.taxi.common.dto.DriverResponseDto;
import com.taxi.common.enums.DriverStatus;
import com.taxi.user.entity.Driver;
import com.taxi.user.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverResponseDto> createDriver(@Valid @RequestBody DriverDto dto) {
        Driver driver = driverService.createDriver(dto);
        return ResponseEntity.ok(toDto(driver));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseDto> getDriver(@PathVariable Long id) {
        Driver driver = driverService.getDriverById(id);
        return ResponseEntity.ok(toDto(driver));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DriverResponseDto> updateDriverStatus(
            @PathVariable Long id,
            @RequestParam DriverStatus status) {
        Driver driver = driverService.updateDriverStatus(id, status);
        return ResponseEntity.ok(toDto(driver));
    }

    @GetMapping("/available/first")
    public ResponseEntity<DriverResponseDto> findAvailableDriver() {
        Driver driver = driverService.findAndReserveAvailableDriver();
        return ResponseEntity.ok(toDto(driver));
    }

    private DriverResponseDto toDto(Driver driver) {
        DriverResponseDto dto = new DriverResponseDto();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setEmail(driver.getEmail());
        dto.setPhone(driver.getPhone());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setStatus(driver.getStatus());
        return dto;
    }
}