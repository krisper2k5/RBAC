package com.taxi.common.dto;

import com.taxi.common.enums.DriverStatus;
import lombok.Data;

@Data
public class DriverResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String licenseNumber;
    private DriverStatus status;
}