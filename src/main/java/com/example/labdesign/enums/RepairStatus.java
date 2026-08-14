package com.example.labdesign.enums;

public enum RepairStatus {
    PENDING("待处理"),
    PROCESSING("维修中"),
    COMPLETED("已完成");

    private final String label;

    RepairStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
