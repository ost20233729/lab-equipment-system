package com.example.labdesign.controller;

import com.example.labdesign.dto.ApprovalRequest;
import com.example.labdesign.dto.BorrowCreateRequest;
import com.example.labdesign.dto.BorrowResponse;
import com.example.labdesign.dto.RejectRequest;
import com.example.labdesign.dto.ReturnRequest;
import com.example.labdesign.entity.AppUser;
import com.example.labdesign.service.BorrowService;
import com.example.labdesign.service.UserContextService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 借用申请相关接口入口。
 */
@RestController
@RequestMapping("/api/borrows")
public class BorrowController {
    private final BorrowService borrowService;
    private final UserContextService userContextService;

    public BorrowController(BorrowService borrowService, UserContextService userContextService) {
        this.borrowService = borrowService;
        this.userContextService = userContextService;
    }

    @GetMapping
    public List<BorrowResponse> list(@RequestHeader("X-User-Id") Long userId,
                                     @RequestParam(defaultValue = "false") boolean mine) {
        // 通过请求头中的演示用户编号切换当前操作者。
        AppUser user = userContextService.requireUser(userId);
        return borrowService.list(user, mine).stream().map(BorrowResponse::from).toList();
    }

    @PostMapping
    public BorrowResponse create(@RequestHeader("X-User-Id") Long userId,
                                 @Valid @RequestBody BorrowCreateRequest request) {
        AppUser user = userContextService.requireUser(userId);
        return BorrowResponse.from(borrowService.create(request, user));
    }

    @PostMapping("/{id}/approve")
    public BorrowResponse approve(@PathVariable Long id,
                                  @RequestHeader("X-User-Id") Long userId,
                                  @RequestBody(required = false) ApprovalRequest request) {
        AppUser user = userContextService.requireUser(userId);
        // 审批意见是可选参数，未填写时由 Service 生成默认说明。
        String comment = request == null ? null : request.comment();
        return BorrowResponse.from(borrowService.approve(id, comment, user));
    }

    @PostMapping("/{id}/reject")
    public BorrowResponse reject(@PathVariable Long id,
                                 @RequestHeader("X-User-Id") Long userId,
                                 @Valid @RequestBody RejectRequest request) {
        AppUser user = userContextService.requireUser(userId);
        return BorrowResponse.from(borrowService.reject(id, request.reason(), user));
    }

    @PostMapping("/{id}/return")
    public BorrowResponse returnEquipment(@PathVariable Long id,
                                          @RequestHeader("X-User-Id") Long userId,
                                          @Valid @RequestBody ReturnRequest request) {
        AppUser user = userContextService.requireUser(userId);
        return BorrowResponse.from(borrowService.returnEquipment(id, request, user));
    }
}
