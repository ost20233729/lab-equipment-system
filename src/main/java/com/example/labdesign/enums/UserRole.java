package com.example.labdesign.enums;

public enum UserRole {
    STUDENT(0, "学生"),
    LAB_ADMIN(1, "实验室管理员"),
    TEACHER(2, "指导教师"),
    DEAN(3, "学院负责人");

    private final int approvalRank;
    private final String label;

    UserRole(int approvalRank, String label) {
        this.approvalRank = approvalRank;
        this.label = label;
    }

    public int getApprovalRank() {
        return approvalRank;
    }

    public String getLabel() {
        return label;
    }

    public boolean canApprove(UserRole requiredRole) {
        return approvalRank >= requiredRole.approvalRank;
    }

    public boolean canManageEquipment() {
        return this == LAB_ADMIN || this == TEACHER || this == DEAN;
    }
}
