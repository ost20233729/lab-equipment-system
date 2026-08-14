package com.example.labdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RepairCreateRequest(@NotNull Long equipmentId, @NotBlank String faultDescription) {
}
