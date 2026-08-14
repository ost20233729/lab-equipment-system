package com.example.labdesign.enums;

public enum ApprovalAction {
    REQUESTED("提交申请"),
    APPROVED("审批通过"),
    REJECTED("审批驳回"),
    RETURNED("归还登记");

    private final String label;

    ApprovalAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
