package com.example.labdesign.pattern.fee;

import java.math.BigDecimal;

public class ComputerEquipmentFeeStrategy implements OverdueFeeStrategy {
    @Override
    public BigDecimal calculate(long overdueDays) {
        return BigDecimal.valueOf(Math.max(overdueDays, 0)).multiply(new BigDecimal("5.00"));
    }
}
