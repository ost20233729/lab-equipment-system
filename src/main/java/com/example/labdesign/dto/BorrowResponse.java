package com.example.labdesign.dto;

import com.example.labdesign.entity.BorrowRequest;
import com.example.labdesign.enums.BorrowStatus;
import com.example.labdesign.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BorrowResponse(
        Long id,
        Long equipmentId,
        String equipmentName,
        String equipmentCode,
        String applicantName,
        String approverName,
        LocalDate startDate,
        LocalDate expectedReturnDate,
        LocalDate actualReturnDate,
        String purpose,
        BorrowStatus status,
        String statusLabel,
        UserRole requiredApproverRole,
        String requiredApproverRoleLabel,
        String rejectReason,
        BigDecimal overdueFee,
        LocalDateTime createdAt
) {
    public static BorrowResponse from(BorrowRequest request) {
        return new BorrowResponse(
                request.getId(),
                request.getEquipment().getId(),
                request.getEquipment().getName(),
                request.getEquipment().getCode(),
                request.getApplicant().getRealName(),
                request.getApprover() == null ? "" : request.getApprover().getRealName(),
                request.getStartDate(),
                request.getExpectedReturnDate(),
                request.getActualReturnDate(),
                request.getPurpose(),
                request.getStatus(),
                request.getStatus().getLabel(),
                request.getRequiredApproverRole(),
                request.getRequiredApproverRole().getLabel(),
                request.getRejectReason(),
                request.getOverdueFee(),
                request.getCreatedAt()
        );
    }
}
