package com.taxi.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DriverDto {
    @NotBlank
    private String name;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String phone;
    @NotBlank
    private String licenseNumber;
}