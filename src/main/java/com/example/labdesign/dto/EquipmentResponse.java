package com.example.labdesign.dto;

import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.EquipmentCategory;
import com.example.labdesign.enums.EquipmentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EquipmentResponse(
        Long id,
        String code,
        String name,
        EquipmentCategory category,
        String categoryLabel,
        EquipmentStatus status,
        String statusLabel,
        String labRoom,
        BigDecimal value,
        String manager,
        String description,
        LocalDateTime updatedAt
) {
    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getCode(),
                equipment.getName(),
                equipment.getCategory(),
                equipment.getCategory().getLabel(),
                equipment.getStatus(),
                equipment.getStatus().getLabel(),
                equipment.getLabRoom(),
                equipment.getValue(),
                equipment.getManager(),
                equipment.getDescription(),
                equipment.getUpdatedAt()
        );
    }
}
