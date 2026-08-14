package com.example.labdesign.service;

import com.example.labdesign.entity.AppUser;
import com.example.labdesign.entity.NotificationMessage;
import com.example.labdesign.repository.NotificationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处理通知查询与已读标记。
 */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationMessage> list(AppUser user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Transactional
    public NotificationMessage markRead(Long id, AppUser user) {
        NotificationMessage message = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("通知不存在：" + id));
        // 只允许接收人本人操作通知，避免越权修改已读状态。
        if (!message.getRecipient().getId().equals(user.getId())) {
            throw new BusinessException("不能操作其他用户的通知");
        }
        message.setReadFlag(true);
        return notificationRepository.save(message);
    }
}
