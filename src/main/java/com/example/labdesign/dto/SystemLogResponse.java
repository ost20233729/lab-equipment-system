package com.example.labdesign.dto;

import com.example.labdesign.entity.SystemLog;
import com.example.labdesign.enums.EventType;
import java.time.LocalDateTime;

public record SystemLogResponse(Long id, EventType eventType, String content, LocalDateTime createdAt) {
    public static SystemLogResponse from(SystemLog log) {
        return new SystemLogResponse(log.getId(), log.getEventType(), log.getContent(), log.getCreatedAt());
    }
}
