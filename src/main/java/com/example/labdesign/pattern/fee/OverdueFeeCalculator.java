package com.example.labdesign.pattern.fee;

import com.example.labdesign.enums.EquipmentCategory;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * 根据设备类别选择不同的逾期费用策略。
 */
@Component
public class OverdueFeeCalculator {
    public BigDecimal calculate(EquipmentCategory category, long overdueDays) {
        return strategyOf(category).calculate(overdueDays);
    }

    private OverdueFeeStrategy strategyOf(EquipmentCategory category) {
        // 不同类别设备的风险和使用成本不同，因此采用不同费率策略。
        return switch (category) {
            case NORMAL -> new NormalEquipmentFeeStrategy();
            case COMPUTER -> new ComputerEquipmentFeeStrategy();
            case PRECISION -> new PrecisionEquipmentFeeStrategy();
        };
    }
}
