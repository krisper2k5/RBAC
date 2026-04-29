package com.taxi.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TripDto {
    @NotNull
    private Long passengerId;
    @NotBlank
    private String origin;
    @NotBlank
    private String destination;
    @Positive
    private double distanceKm; // для расчёта цены
}