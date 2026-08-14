package com.example.labdesign.enums;

public enum EquipmentStatus {
    AVAILABLE("可借用"),
    BORROWED("已借出"),
    REPAIRING("维修中"),
    SCRAPPED("已报废");

    private final String label;

    EquipmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
