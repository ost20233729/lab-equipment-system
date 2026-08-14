package com.example.labdesign.controller;

import com.example.labdesign.dto.EquipmentCreateRequest;
import com.example.labdesign.dto.EquipmentResponse;
import com.example.labdesign.entity.AppUser;
import com.example.labdesign.service.EquipmentService;
import com.example.labdesign.service.UserContextService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备管理相关接口入口。
 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService equipmentService;
    private final UserContextService userContextService;

    public EquipmentController(EquipmentService equipmentService, UserContextService userContextService) {
        this.equipmentService = equipmentService;
        this.userContextService = userContextService;
    }

    @GetMapping
    public List<EquipmentResponse> list(@RequestParam(required = false) String keyword) {
        // 设备列表对所有演示角色开放，支持按关键字筛选。
        return equipmentService.list(keyword).stream().map(EquipmentResponse::from).toList();
    }

    @PostMapping
    public EquipmentResponse create(@RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody EquipmentCreateRequest request) {
        AppUser user = userContextService.requireUser(userId);
        return EquipmentResponse.from(equipmentService.create(request, user));
    }

    @PutMapping("/{id}")
    public EquipmentResponse update(@PathVariable Long id,
                                    @RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody EquipmentCreateRequest request) {
        AppUser user = userContextService.requireUser(userId);
        return EquipmentResponse.from(equipmentService.update(id, request, user));
    }

    @PostMapping("/{id}/scrap")
    public EquipmentResponse scrap(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        AppUser user = userContextService.requireUser(userId);
        return EquipmentResponse.from(equipmentService.scrap(id, user));
    }
}
