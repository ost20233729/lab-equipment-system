package com.example.labdesign.service;

import com.example.labdesign.dto.RepairCompleteRequest;
import com.example.labdesign.dto.RepairCreateRequest;
import com.example.labdesign.entity.AppUser;
import com.example.labdesign.entity.Equipment;
import com.example.labdesign.entity.RepairTicket;
import com.example.labdesign.enums.EventType;
import com.example.labdesign.enums.RepairStatus;
import com.example.labdesign.pattern.event.DomainEvent;
import com.example.labdesign.pattern.event.EventPublisher;
import com.example.labdesign.pattern.state.EquipmentStateFactory;
import com.example.labdesign.repository.EquipmentRepository;
import com.example.labdesign.repository.RepairTicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处理设备报修工单的创建、开始维修与维修完成。
 */
@Service
public class RepairService {
    private final RepairTicketRepository repairTicketRepository;
    private final EquipmentService equipmentService;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStateFactory stateFactory;
    private final EventPublisher eventPublisher;

    public RepairService(RepairTicketRepository repairTicketRepository,
                         EquipmentService equipmentService,
                         EquipmentRepository equipmentRepository,
                         EquipmentStateFactory stateFactory,
                         EventPublisher eventPublisher) {
        this.repairTicketRepository = repairTicketRepository;
        this.equipmentService = equipmentService;
        this.equipmentRepository = equipmentRepository;
        this.stateFactory = stateFactory;
        this.eventPublisher = eventPublisher;
    }

    public List<RepairTicket> list() {
        return repairTicketRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public RepairTicket create(RepairCreateRequest request, AppUser reporter) {
        Equipment equipment = equipmentService.requireEquipment(request.equipmentId());
        // 报修后设备状态切换为维修中，防止继续被借出。
        stateFactory.getState(equipment.getStatus()).reportRepair(equipment);
        equipmentRepository.save(equipment);
        RepairTicket ticket = new RepairTicket();
        ticket.setEquipment(equipment);
        ticket.setReporter(reporter);
        ticket.setFaultDescription(request.faultDescription());
        RepairTicket saved = repairTicketRepository.save(ticket);
        eventPublisher.publish(new DomainEvent(
                EventType.REPAIR_SUBMITTED,
                reporter,
                "报修已提交",
                "设备 " + equipment.getName() + " 已进入维修流程"
        ));
        return saved;
    }

    @Transactional
    public RepairTicket start(Long id, AppUser handler) {
        if (!handler.getRole().canManageEquipment()) {
            throw new BusinessException("当前用户无权处理维修工单");
        }
        RepairTicket ticket = requireTicket(id);
        // 只有待处理工单才允许开始维修，避免重复开始。
        if (ticket.getStatus() != RepairStatus.PENDING) {
            throw new BusinessException("只有待处理工单可以开始维修");
        }
        ticket.setStatus(RepairStatus.PROCESSING);
        ticket.setHandler(handler);
        RepairTicket saved = repairTicketRepository.save(ticket);
        eventPublisher.publish(new DomainEvent(
                EventType.REPAIR_STARTED,
                ticket.getReporter(),
                "维修已开始",
                "设备 " + ticket.getEquipment().getName() + " 已由 " + handler.getRealName() + " 开始维修"
        ));
        return saved;
    }

    @Transactional
    public RepairTicket complete(Long id, RepairCompleteRequest request, AppUser handler) {
        if (!handler.getRole().canManageEquipment()) {
            throw new BusinessException("当前用户无权完成维修工单");
        }
        RepairTicket ticket = requireTicket(id);
        // 已完成工单不能二次处理，避免覆盖维修结论。
        if (ticket.getStatus() == RepairStatus.COMPLETED) {
            throw new BusinessException("工单已完成，不能重复处理");
        }
        Equipment equipment = ticket.getEquipment();
        // 维修完成后通过状态模式把设备恢复到可借用状态。
        stateFactory.getState(equipment.getStatus()).finishRepair(equipment);
        equipmentRepository.save(equipment);
        ticket.setStatus(RepairStatus.COMPLETED);
        ticket.setHandler(handler);
        ticket.setRepairResult(request.repairResult());
        ticket.setCompletedAt(LocalDateTime.now());
        RepairTicket saved = repairTicketRepository.save(ticket);
        eventPublisher.publish(new DomainEvent(
                EventType.REPAIR_COMPLETED,
                ticket.getReporter(),
                "维修已完成",
                "设备 " + equipment.getName() + " 已恢复可借用，处理结果：" + request.repairResult()
        ));
        return saved;
    }

    private RepairTicket requireTicket(Long id) {
        return repairTicketRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维修工单不存在：" + id));
    }
}
