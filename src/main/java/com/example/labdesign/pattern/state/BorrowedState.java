package com.example.labdesign.pattern.state;

import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.EquipmentStatus;

public class BorrowedState extends AbstractEquipmentState {
    @Override
    public void returnBack(Equipment equipment) {
        equipment.setStatus(EquipmentStatus.AVAILABLE);
    }

    @Override
    public void reportRepair(Equipment equipment) {
        equipment.setStatus(EquipmentStatus.REPAIRING);
    }
}
