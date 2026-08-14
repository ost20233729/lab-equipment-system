package com.example.labdesign.pattern.event;

public interface EventObserver {
    void onEvent(DomainEvent event);
}
