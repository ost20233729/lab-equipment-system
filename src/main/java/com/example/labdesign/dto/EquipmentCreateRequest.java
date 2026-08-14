package com.example.labdesign.dto;

import com.example.labdesign.enums.EquipmentCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EquipmentCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull EquipmentCategory category,
        @NotBlank String labRoom,
        @NotNull @DecimalMin("0.0") BigDecimal value,
        String manager,
        String description
) {
}
