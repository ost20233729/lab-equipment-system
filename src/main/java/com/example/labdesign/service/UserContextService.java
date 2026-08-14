package com.example.labdesign.service;

import com.example.labdesign.entity.AppUser;
import com.example.labdesign.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {
    private final AppUserRepository appUserRepository;

    public UserContextService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("缺少演示用户编号，请在请求头 X-User-Id 中传入用户 ID");
        }
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("演示用户不存在：" + userId));
    }
}
