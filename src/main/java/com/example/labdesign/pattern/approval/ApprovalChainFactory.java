package com.example.labdesign.pattern.approval;

import org.springframework.stereotype.Component;

/**
 * 构建设备借用审批职责链。
 */
@Component
public class ApprovalChainFactory {
    public ApprovalHandler createChain() {
        // 先由实验室管理员判断，必要时再升级到指导教师和院领导。
        ApprovalHandler labAdmin = new LabAdminApprovalHandler();
        ApprovalHandler teacher = labAdmin.linkWith(new TeacherApprovalHandler());
        teacher.linkWith(new DeanApprovalHandler());
        return labAdmin;
    }
}
