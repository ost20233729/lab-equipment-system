package com.example.labdesign.pattern.state;

import com.example.labdesign.entity.Equipment;

public interface EquipmentState {
    void borrow(Equipment equipment);

    void returnBack(Equipment equipment);

    void reportRepair(Equipment equipment);

    void finishRepair(Equipment equipment);

    void scrap(Equipment equipment);
}
