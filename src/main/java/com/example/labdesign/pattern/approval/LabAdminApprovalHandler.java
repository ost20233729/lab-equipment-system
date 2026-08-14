package com.example.labdesign.pattern.approval;

import com.example.labdesign.enums.EquipmentCategory;
import com.example.labdesign.enums.UserRole;
import java.math.BigDecimal;

public class LabAdminApprovalHandler extends ApprovalHandler {
    @Override
    protected boolean canHandle(ApprovalContext context) {
        return context.getBorrowDays() <= 3
                && context.getEquipment().getCategory() == EquipmentCategory.NORMAL
                && context.getEquipment().getValue().compareTo(new BigDecimal("5000")) <= 0;
    }

    @Override
    protected UserRole getRole() {
        return UserRole.LAB_ADMIN;
    }

    @Override
    protected String buildReason(ApprovalContext context) {
        return "普通设备短期借用，由实验室管理员审批";
    }
}
