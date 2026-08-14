package com.example.labdesign.pattern.event;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 观察者模式中的事件发布器，负责将领域事件广播给所有观察者。
 */
@Component
public class EventPublisher {
    private final List<EventObserver> observers;

    public EventPublisher(List<EventObserver> observers) {
        this.observers = observers;
    }

    public void publish(DomainEvent event) {
        // 发布器本身不处理副作用，只负责触发通知和日志等后续动作。
        observers.forEach(observer -> observer.onEvent(event));
    }
}
