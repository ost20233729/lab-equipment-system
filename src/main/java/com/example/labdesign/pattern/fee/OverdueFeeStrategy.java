package com.example.labdesign.pattern.fee;

import java.math.BigDecimal;

public interface OverdueFeeStrategy {
    BigDecimal calculate(long overdueDays);
}
