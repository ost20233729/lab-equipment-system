package com.example.labdesign.dto;

import com.example.labdesign.entity.NotificationMessage;
import java.time.LocalDateTime;

public record NotificationResponse(Long id, String title, String content, boolean readFlag, LocalDateTime createdAt) {
    public static NotificationResponse from(NotificationMessage message) {
        return new NotificationResponse(
                message.getId(),
                message.getTitle(),
                message.getContent(),
                message.isReadFlag(),
                message.getCreatedAt()
        );
    }
}
