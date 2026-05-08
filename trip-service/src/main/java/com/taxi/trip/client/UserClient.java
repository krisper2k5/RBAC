package com.taxi.trip.client;

import com.taxi.common.dto.DriverResponseDto;
import com.taxi.common.enums.DriverStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "user-service",
        url = "${user.service.url:http://user-service:8080}",
        configuration = com.taxi.trip.config.FeignConfig.class
)
public interface UserClient {

    @GetMapping("/drivers/available/first")
    DriverResponseDto findAvailableDriver();

    @PatchMapping("/drivers/{id}/status")
    DriverResponseDto updateDriverStatus(
            @PathVariable Long id,
            @RequestParam DriverStatus status
    );

    @GetMapping("/passengers/{id}")
    void validatePassengerExists(@PathVariable Long id);
}