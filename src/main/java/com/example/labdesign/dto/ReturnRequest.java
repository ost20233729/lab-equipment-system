package com.example.labdesign.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReturnRequest(@NotNull LocalDate actualReturnDate) {
}
