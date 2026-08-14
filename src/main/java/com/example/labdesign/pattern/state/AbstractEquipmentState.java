package com.example.labdesign.pattern.state;

import com.example.labdesign.entity.Equipment;

public abstract class AbstractEquipmentState implements EquipmentState {
    @Override
    public void borrow(Equipment equipment) {
        throw new IllegalStateException("当前状态不允许借用设备");
    }

    @Override
    public void returnBack(Equipment equipment) {
        throw new IllegalStateException("当前状态不允许归还设备");
    }

    @Override
    public void reportRepair(Equipment equipment) {
        throw new IllegalStateException("当前状态不允许报修设备");
    }

    @Override
    public void finishRepair(Equipment equipment) {
        throw new IllegalStateException("当前状态不允许完成维修");
    }

    @Override
    public void scrap(Equipment equipment) {
        throw new IllegalStateException("当前状态不允许报废设备");
    }
}
