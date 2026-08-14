package com.example.labdesign.pattern.state;

import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.EquipmentStatus;

public class RepairingState extends AbstractEquipmentState {
    @Override
    public void finishRepair(Equipment equipment) {
        equipment.setStatus(EquipmentStatus.AVAILABLE);
    }

    @Override
    public void scrap(Equipment equipment) {
        equipment.setStatus(EquipmentStatus.SCRAPPED);
    }
}
