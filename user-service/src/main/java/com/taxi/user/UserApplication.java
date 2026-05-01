package com.taxi.user;

import com.taxi.user.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class UserApplication {

    private final DriverService driverService;

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Bean
    public ApplicationRunner initRedisCache() {
        return args -> driverService.syncAvailableDriversCache();
    }
}