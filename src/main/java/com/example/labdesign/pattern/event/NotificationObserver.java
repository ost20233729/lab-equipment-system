package com.example.labdesign.pattern.event;

import com.example.labdesign.entity.NotificationMessage;
import com.example.labdesign.repository.NotificationRepository;
import org.springframework.stereotype.Component;

/**
 * 监听业务事件并生成站内通知。
 */
@Component
public class NotificationObserver implements EventObserver {
    private final NotificationRepository notificationRepository;

    public NotificationObserver(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void onEvent(DomainEvent event) {
        // 没有接收人的事件只写日志，不生成通知。
        if (event.recipient() == null) {
            return;
        }
        NotificationMessage message = new NotificationMessage();
        message.setRecipient(event.recipient());
        message.setTitle(event.title());
        message.setContent(event.content());
        notificationRepository.save(message);
    }
}
