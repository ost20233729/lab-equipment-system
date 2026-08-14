package com.example.labdesign.pattern.approval;

import com.example.labdesign.enums.UserRole;

public record ApprovalDecision(UserRole requiredRole, String reason) {
}
