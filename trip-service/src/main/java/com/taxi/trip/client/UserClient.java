package com.taxi.trip.client;

import com.taxi.common.dto.DriverResponseDto;
import com.taxi.common.enums.DriverStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8081}")
public interface UserClient {

    @GetMapping("/drivers/available/first")
    DriverResponseDto findAvailableDriver();

    @PatchMapping("/drivers/{id}/status")
    DriverResponseDto updateDriverStatus(
            @PathVariable Long id,
            @RequestParam DriverStatus status
    );
}