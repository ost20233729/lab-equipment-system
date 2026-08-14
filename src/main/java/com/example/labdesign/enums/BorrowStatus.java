package com.example.labdesign.enums;

public enum BorrowStatus {
    PENDING("待审批"),
    APPROVED("已通过"),
    REJECTED("已驳回"),
    RETURNED("已归还");

    private final String label;

    BorrowStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
