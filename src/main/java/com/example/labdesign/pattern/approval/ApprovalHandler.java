package com.example.labdesign.pattern.approval;

import com.example.labdesign.enums.UserRole;

public abstract class ApprovalHandler {
    private ApprovalHandler next;

    public ApprovalHandler linkWith(ApprovalHandler next) {
        this.next = next;
        return next;
    }

    public ApprovalDecision decide(ApprovalContext context) {
        if (canHandle(context)) {
            return new ApprovalDecision(getRole(), buildReason(context));
        }
        if (next == null) {
            return new ApprovalDecision(UserRole.DEAN, "超过普通审批范围，需学院负责人审批");
        }
        return next.decide(context);
    }

    protected abstract boolean canHandle(ApprovalContext context);

    protected abstract UserRole getRole();

    protected abstract String buildReason(ApprovalContext context);
}
