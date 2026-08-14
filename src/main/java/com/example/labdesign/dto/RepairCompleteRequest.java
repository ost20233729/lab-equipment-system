package com.example.labdesign.dto;

import jakarta.validation.constraints.NotBlank;

public record RepairCompleteRequest(@NotBlank String repairResult) {
}
