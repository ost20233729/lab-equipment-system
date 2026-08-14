package com.example.labdesign.dto;

import com.example.labdesign.entity.RepairTicket;
import com.example.labdesign.enums.RepairStatus;
import java.time.LocalDateTime;

public record RepairResponse(
        Long id,
        Long equipmentId,
        String equipmentName,
        String equipmentCode,
        String reporterName,
        String handlerName,
        String faultDescription,
        String repairResult,
        RepairStatus status,
        String statusLabel,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
    public static RepairResponse from(RepairTicket ticket) {
        return new RepairResponse(
                ticket.getId(),
                ticket.getEquipment().getId(),
                ticket.getEquipment().getName(),
                ticket.getEquipment().getCode(),
                ticket.getReporter().getRealName(),
                ticket.getHandler() == null ? "" : ticket.getHandler().getRealName(),
                ticket.getFaultDescription(),
                ticket.getRepairResult(),
                ticket.getStatus(),
                ticket.getStatus().getLabel(),
                ticket.getCreatedAt(),
                ticket.getCompletedAt()
        );
    }
}
