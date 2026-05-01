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
        return ResponseEntity.ok(toDto(driverService.createDriver(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseDto> getDriver(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(driverService.getDriverById(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DriverResponseDto> updateDriverStatus(@PathVariable Long id, @RequestParam DriverStatus status) {
        return ResponseEntity.ok(toDto(driverService.updateDriverStatus(id, status)));
    }

    @GetMapping("/available/first")
    public ResponseEntity<DriverResponseDto> findAvailableDriver() {
        return ResponseEntity.ok(toDto(driverService.findAndReserveAvailableDriver()));
    }

    private DriverResponseDto toDto(Driver d) {
        DriverResponseDto dto = new DriverResponseDto();
        dto.setId(d.getId()); dto.setName(d.getName()); dto.setEmail(d.getEmail());
        dto.setPhone(d.getPhone()); dto.setLicenseNumber(d.getLicenseNumber()); dto.setStatus(d.getStatus());
        return dto;
    }
}