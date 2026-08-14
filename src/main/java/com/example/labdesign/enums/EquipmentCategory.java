package com.example.labdesign.enums;

public enum EquipmentCategory {
    NORMAL("普通设备"),
    COMPUTER("计算机设备"),
    PRECISION("精密仪器");

    private final String label;

    EquipmentCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
