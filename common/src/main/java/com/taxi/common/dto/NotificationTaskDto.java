package com.taxi.common.dto;

import com.taxi.common.enums.RecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationTaskDto {
    @NotNull
    private Long tripId;
    @NotNull
    private RecipientType recipientType;
    @NotNull
    private Long recipientId;
    @NotBlank
    private String message;
}