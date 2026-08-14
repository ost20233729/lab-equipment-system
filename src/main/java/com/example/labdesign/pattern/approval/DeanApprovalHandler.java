package com.example.labdesign.pattern.approval;

import com.example.labdesign.enums.UserRole;

public class DeanApprovalHandler extends ApprovalHandler {
    @Override
    protected boolean canHandle(ApprovalContext context) {
        return true;
    }

    @Override
    protected UserRole getRole() {
        return UserRole.DEAN;
    }

    @Override
    protected String buildReason(ApprovalContext context) {
        return "精密仪器、高价值或长期借用，由学院负责人审批";
    }
}
