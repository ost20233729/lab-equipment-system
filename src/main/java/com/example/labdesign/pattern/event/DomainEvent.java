package com.example.labdesign.pattern.event;

import com.example.labdesign.entity.AppUser;
import com.example.labdesign.enums.EventType;

public record DomainEvent(EventType type, AppUser recipient, String title, String content) {
}
