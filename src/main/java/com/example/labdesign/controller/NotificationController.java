package com.example.labdesign.controller;

import com.example.labdesign.dto.NotificationResponse;
import com.example.labdesign.dto.SystemLogResponse;
import com.example.labdesign.entity.AppUser;
import com.example.labdesign.repository.SystemLogRepository;
import com.example.labdesign.service.NotificationService;
import com.example.labdesign.service.UserContextService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知与系统日志相关接口入口。
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserContextService userContextService;
    private final SystemLogRepository systemLogRepository;

    public NotificationController(NotificationService notificationService,
                                  UserContextService userContextService,
                                  SystemLogRepository systemLogRepository) {
        this.notificationService = notificationService;
        this.userContextService = userContextService;
        this.systemLogRepository = systemLogRepository;
    }

    @GetMapping
    public List<NotificationResponse> list(@RequestHeader("X-User-Id") Long userId) {
        // 通知始终按当前登录用户维度查询。
        AppUser user = userContextService.requireUser(userId);
        return notificationService.list(user).stream().map(NotificationResponse::from).toList();
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        AppUser user = userContextService.requireUser(userId);
        return NotificationResponse.from(notificationService.markRead(id, user));
    }

    @GetMapping("/logs")
    public List<SystemLogResponse> logs() {
        // 只返回最新 50 条日志，避免日志表无限增长后影响前端加载。
        return systemLogRepository.findTop50ByOrderByCreatedAtDesc().stream().map(SystemLogResponse::from).toList();
    }
}
