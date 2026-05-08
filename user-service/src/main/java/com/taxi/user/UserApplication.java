package com.taxi.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan; // ← Добавить импорт

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.taxi.user",
        "com.taxi.common"
})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}