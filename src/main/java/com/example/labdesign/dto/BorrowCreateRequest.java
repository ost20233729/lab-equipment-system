package com.example.labdesign.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BorrowCreateRequest(
        @NotNull Long equipmentId,
        @NotNull @FutureOrPresent LocalDate startDate,
        @NotNull @FutureOrPresent LocalDate expectedReturnDate,
        @NotBlank String purpose
) {
}
