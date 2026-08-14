package com.example.labdesign.controller;

import com.example.labdesign.dto.RepairCompleteRequest;
import com.example.labdesign.dto.RepairCreateRequest;
import com.example.labdesign.dto.RepairResponse;
import com.example.labdesign.entity.AppUser;
import com.example.labdesign.service.RepairService;
import com.example.labdesign.service.UserContextService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报修工单相关接口入口。
 */
@RestController
@RequestMapping("/api/repairs")
public class RepairController {
    private final RepairService repairService;
    private final UserContextService userContextService;

    public RepairController(RepairService repairService, UserContextService userContextService) {
        this.repairService = repairService;
        this.userContextService = userContextService;
    }

    @GetMapping
    public List<RepairResponse> list() {
        return repairService.list().stream().map(RepairResponse::from).toList();
    }

    @PostMapping
    public RepairResponse create(@RequestHeader("X-User-Id") Long userId,
                                 @Valid @RequestBody RepairCreateRequest request) {
        AppUser user = userContextService.requireUser(userId);
        return RepairResponse.from(repairService.create(request, user));
    }

    @PostMapping("/{id}/start")
    public RepairResponse start(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        AppUser user = userContextService.requireUser(userId);
        return RepairResponse.from(repairService.start(id, user));
    }

    @PostMapping("/{id}/complete")
    public RepairResponse complete(@PathVariable Long id,
                                   @RequestHeader("X-User-Id") Long userId,
                                   @Valid @RequestBody RepairCompleteRequest request) {
        AppUser user = userContextService.requireUser(userId);
        return RepairResponse.from(repairService.complete(id, request, user));
    }
}
