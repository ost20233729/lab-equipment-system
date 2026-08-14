package com.example.labdesign.pattern.state;

import com.example.labdesign.enums.EquipmentStatus;
import org.springframework.stereotype.Component;

/**
 * 根据当前设备状态返回对应的状态对象。
 */
@Component
public class EquipmentStateFactory {
    public EquipmentState getState(EquipmentStatus status) {
        // 将借出、归还、报修、报废等规则集中在状态类中统一维护。
        return switch (status) {
            case AVAILABLE -> new AvailableState();
            case BORROWED -> new BorrowedState();
            case REPAIRING -> new RepairingState();
            case SCRAPPED -> new ScrappedState();
        };
    }
}
