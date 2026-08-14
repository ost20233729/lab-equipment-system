package com.example.labdesign.pattern.event;

import com.example.labdesign.entity.SystemLog;
import com.example.labdesign.repository.SystemLogRepository;
import org.springframework.stereotype.Component;

/**
 * 监听业务事件并落库为系统日志。
 */
@Component
public class SystemLogObserver implements EventObserver {
    private final SystemLogRepository systemLogRepository;

    public SystemLogObserver(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @Override
    public void onEvent(DomainEvent event) {
        // 所有业务事件都记录为日志，便于审计和课程演示。
        SystemLog log = new SystemLog();
        log.setEventType(event.type());
        log.setContent(event.title() + "：" + event.content());
        systemLogRepository.save(log);
    }
}
