package com.example.labdesign.pattern.approval;

import com.example.labdesign.enums.EquipmentCategory;
import com.example.labdesign.enums.UserRole;
import java.math.BigDecimal;

public class TeacherApprovalHandler extends ApprovalHandler {
    @Override
    protected boolean canHandle(ApprovalContext context) {
        boolean notPrecision = context.getEquipment().getCategory() != EquipmentCategory.PRECISION;
        boolean allowedDays = context.getBorrowDays() <= 14;
        boolean allowedValue = context.getEquipment().getValue().compareTo(new BigDecimal("20000")) <= 0;
        return notPrecision && allowedDays && allowedValue;
    }

    @Override
    protected UserRole getRole() {
        return UserRole.TEACHER;
    }

    @Override
    protected String buildReason(ApprovalContext context) {
        return "中等价值或较长周期借用，由指导教师审批";
    }
}
