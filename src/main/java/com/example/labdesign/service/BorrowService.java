package com.example.labdesign.service;

import com.example.labdesign.dto.BorrowCreateRequest;
import com.example.labdesign.dto.ReturnRequest;
import com.example.labdesign.entity.AppUser;
import com.example.labdesign.entity.ApprovalRecord;
import com.example.labdesign.entity.BorrowRequest;
import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.ApprovalAction;
import com.example.labdesign.enums.BorrowStatus;
import com.example.labdesign.enums.EquipmentStatus;
import com.example.labdesign.enums.EventType;
import com.example.labdesign.enums.UserRole;
import com.example.labdesign.pattern.approval.ApprovalChainFactory;
import com.example.labdesign.pattern.approval.ApprovalContext;
import com.example.labdesign.pattern.approval.ApprovalDecision;
import com.example.labdesign.pattern.event.DomainEvent;
import com.example.labdesign.pattern.event.EventPublisher;
import com.example.labdesign.pattern.fee.OverdueFeeCalculator;
import com.example.labdesign.pattern.state.EquipmentStateFactory;
import com.example.labdesign.repository.ApprovalRecordRepository;
import com.example.labdesign.repository.BorrowRequestRepository;
import com.example.labdesign.repository.EquipmentRepository;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 处理设备借用申请、审批与归还登记。
 */
@Service
public class BorrowService {
    private final BorrowRequestRepository borrowRequestRepository;
    private final ApprovalRecordRepository approvalRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentService equipmentService;
    private final ApprovalChainFactory approvalChainFactory;
    private final EquipmentStateFactory stateFactory;
    private final OverdueFeeCalculator overdueFeeCalculator;
    private final EventPublisher eventPublisher;

    public BorrowService(BorrowRequestRepository borrowRequestRepository,
                         ApprovalRecordRepository approvalRecordRepository,
                         EquipmentRepository equipmentRepository,
                         EquipmentService equipmentService,
                         ApprovalChainFactory approvalChainFactory,
                         EquipmentStateFactory stateFactory,
                         OverdueFeeCalculator overdueFeeCalculator,
                         EventPublisher eventPublisher) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.approvalRecordRepository = approvalRecordRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentService = equipmentService;
        this.approvalChainFactory = approvalChainFactory;
        this.stateFactory = stateFactory;
        this.overdueFeeCalculator = overdueFeeCalculator;
        this.eventPublisher = eventPublisher;
    }

    public List<BorrowRequest> list(AppUser user, boolean onlyMine) {
        // 学生默认只能查看自己的申请，管理角色可以查看全部记录。
        if (onlyMine || user.getRole() == UserRole.STUDENT) {
            return borrowRequestRepository.findByApplicantOrderByCreatedAtDesc(user);
        }
        return borrowRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public BorrowRequest create(BorrowCreateRequest request, AppUser applicant) {
        if (!request.expectedReturnDate().isAfter(request.startDate())) {
            throw new BusinessException("预计归还日期必须晚于开始日期");
        }
        Equipment equipment = equipmentService.requireEquipment(request.equipmentId());
        // 只有可借用状态的设备才能发起借用申请。
        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new BusinessException("设备当前不可借用，状态为：" + equipment.getStatus().getLabel());
        }
        // 通过职责链按设备类别、价值和借用天数决定所需审批角色。
        ApprovalDecision decision = approvalChainFactory.createChain().decide(
                new ApprovalContext(equipment, request.startDate(), request.expectedReturnDate())
        );
        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setEquipment(equipment);
        borrowRequest.setApplicant(applicant);
        borrowRequest.setStartDate(request.startDate());
        borrowRequest.setExpectedReturnDate(request.expectedReturnDate());
        borrowRequest.setPurpose(request.purpose());
        borrowRequest.setRequiredApproverRole(decision.requiredRole());
        BorrowRequest saved = borrowRequestRepository.save(borrowRequest);
        saveRecord(saved, applicant, ApprovalAction.REQUESTED, decision.reason());
        eventPublisher.publish(new DomainEvent(
                EventType.BORROW_SUBMITTED,
                applicant,
                "借用申请已提交",
                "设备 " + equipment.getName() + " 需要 " + decision.requiredRole().getLabel() + " 审批"
        ));
        return saved;
    }

    @Transactional
    public BorrowRequest approve(Long id, String comment, AppUser approver) {
        BorrowRequest request = requireBorrow(id);
        // 只有待审批申请才能继续进入审批流程。
        if (request.getStatus() != BorrowStatus.PENDING) {
            throw new BusinessException("只有待审批申请可以审批");
        }
        // 审批权限必须满足申请中记录的必需角色。
        if (!approver.getRole().canApprove(request.getRequiredApproverRole())) {
            throw new BusinessException("当前角色无权审批，该申请需要 " + request.getRequiredApproverRole().getLabel());
        }
        Equipment equipment = request.getEquipment();
        // 设备状态流转交给状态模式处理，避免 Service 中散落状态判断。
        stateFactory.getState(equipment.getStatus()).borrow(equipment);
        equipmentRepository.save(equipment);
        request.setStatus(BorrowStatus.APPROVED);
        request.setApprover(approver);
        BorrowRequest saved = borrowRequestRepository.save(request);
        saveRecord(saved, approver, ApprovalAction.APPROVED, StringUtils.hasText(comment) ? comment : "审批通过");
        eventPublisher.publish(new DomainEvent(
                EventType.BORROW_APPROVED,
                request.getApplicant(),
                "借用申请审批通过",
                "设备 " + equipment.getName() + " 已由 " + approver.getRealName() + " 审批通过"
        ));
        return saved;
    }

    @Transactional
    public BorrowRequest reject(Long id, String reason, AppUser approver) {
        BorrowRequest request = requireBorrow(id);
        // 驳回与审批通过一样，都只允许处理待审批申请。
        if (request.getStatus() != BorrowStatus.PENDING) {
            throw new BusinessException("只有待审批申请可以驳回");
        }
        if (!approver.getRole().canApprove(request.getRequiredApproverRole())) {
            throw new BusinessException("当前角色无权驳回，该申请需要 " + request.getRequiredApproverRole().getLabel());
        }
        request.setStatus(BorrowStatus.REJECTED);
        request.setRejectReason(reason);
        request.setApprover(approver);
        BorrowRequest saved = borrowRequestRepository.save(request);
        saveRecord(saved, approver, ApprovalAction.REJECTED, reason);
        eventPublisher.publish(new DomainEvent(
                EventType.BORROW_REJECTED,
                request.getApplicant(),
                "借用申请被驳回",
                "设备 " + request.getEquipment().getName() + " 的借用申请被驳回，原因：" + reason
        ));
        return saved;
    }

    @Transactional
    public BorrowRequest returnEquipment(Long id, ReturnRequest returnRequest, AppUser operator) {
        BorrowRequest request = requireBorrow(id);
        if (request.getStatus() != BorrowStatus.APPROVED) {
            throw new BusinessException("只有已通过且未归还的申请可以登记归还");
        }
        // 归还既可以由设备管理员登记，也允许申请人本人登记。
        if (!operator.getRole().canManageEquipment() && !request.getApplicant().getId().equals(operator.getId())) {
            throw new BusinessException("当前用户无权登记该设备归还");
        }
        Equipment equipment = request.getEquipment();
        // 逾期费用由策略模式根据设备类别分别计算。
        long overdueDays = Math.max(0, ChronoUnit.DAYS.between(request.getExpectedReturnDate(), returnRequest.actualReturnDate()));
        BigDecimal overdueFee = overdueFeeCalculator.calculate(equipment.getCategory(), overdueDays);
        stateFactory.getState(equipment.getStatus()).returnBack(equipment);
        equipmentRepository.save(equipment);
        request.setStatus(BorrowStatus.RETURNED);
        request.setActualReturnDate(returnRequest.actualReturnDate());
        request.setOverdueFee(overdueFee);
        BorrowRequest saved = borrowRequestRepository.save(request);
        saveRecord(saved, operator, ApprovalAction.RETURNED, "归还登记，逾期费用 " + overdueFee + " 元");
        eventPublisher.publish(new DomainEvent(
                EventType.EQUIPMENT_RETURNED,
                request.getApplicant(),
                "设备已归还",
                "设备 " + equipment.getName() + " 已归还，逾期费用 " + overdueFee + " 元"
        ));
        return saved;
    }

    public BorrowRequest requireBorrow(Long id) {
        return borrowRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("借用申请不存在：" + id));
    }

    private void saveRecord(BorrowRequest request, AppUser operator, ApprovalAction action, String comment) {
        ApprovalRecord record = new ApprovalRecord();
        // 每次状态流转都写入审批记录，便于追溯借用过程。
        record.setBorrowRequest(request);
        record.setOperator(operator);
        record.setAction(action);
        record.setComment(comment);
        approvalRecordRepository.save(record);
    }
}
