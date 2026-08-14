package com.example.labdesign.controller;

import com.example.labdesign.dto.UserResponse;
import com.example.labdesign.repository.AppUserRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public List<UserResponse> list() {
        return appUserRepository.findAll().stream().map(UserResponse::from).toList();
    }
}
