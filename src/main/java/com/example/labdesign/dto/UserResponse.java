package com.example.labdesign.dto;

import com.example.labdesign.entity.AppUser;
import com.example.labdesign.enums.UserRole;

public record UserResponse(Long id, String username, String realName, UserRole role, String roleLabel, String department) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                user.getRole().getLabel(),
                user.getDepartment()
        );
    }
}
