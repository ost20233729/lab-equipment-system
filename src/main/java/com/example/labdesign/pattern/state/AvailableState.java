package com.example.labdesign.pattern.state;

import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.EquipmentStatus;

public class AvailableState extends AbstractEquipmentState {
    @Override
    public void borrow(Equipment equipment) {
        equipment.setStatus(EquipmentStatus.BORROWED);
    }

    @Override
    public void reportRepair(Equipment equipment) {
        equipment.setStatus(EquipmentStatus.REPAIRING);
    }

    @Override
    public void scrap(Equipment equipment) {
        equipment.setStatus(EquipmentStatus.SCRAPPED);
    }
}
