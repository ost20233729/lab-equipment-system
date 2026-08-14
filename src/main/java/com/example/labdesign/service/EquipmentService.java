package com.example.labdesign.service;

import com.example.labdesign.dto.EquipmentCreateRequest;
import com.example.labdesign.entity.AppUser;
import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.EquipmentStatus;
import com.example.labdesign.enums.EventType;
import com.example.labdesign.pattern.event.DomainEvent;
import com.example.labdesign.pattern.event.EventPublisher;
import com.example.labdesign.pattern.state.EquipmentStateFactory;
import com.example.labdesign.repository.EquipmentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 处理设备的查询、新增、修改和报废。
 */
@Service
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStateFactory stateFactory;
    private final EventPublisher eventPublisher;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            EquipmentStateFactory stateFactory,
                            EventPublisher eventPublisher) {
        this.equipmentRepository = equipmentRepository;
        this.stateFactory = stateFactory;
        this.eventPublisher = eventPublisher;
    }

    public List<Equipment> list(String keyword) {
        // 支持按设备名称或设备编号搜索，便于前端快速定位设备。
        if (StringUtils.hasText(keyword)) {
            return equipmentRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(keyword, keyword);
        }
        return equipmentRepository.findAll();
    }

    public Equipment requireEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在：" + id));
    }

    @Transactional
    public Equipment create(EquipmentCreateRequest request, AppUser operator) {
        if (!operator.getRole().canManageEquipment()) {
            throw new BusinessException("当前用户无权新增设备");
        }
        Equipment equipment = new Equipment();
        // 新增设备的默认状态由实体默认值负责，Service 仅填充业务字段。
        fillEquipment(equipment, request);
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment update(Long id, EquipmentCreateRequest request, AppUser operator) {
        if (!operator.getRole().canManageEquipment()) {
            throw new BusinessException("当前用户无权修改设备");
        }
        Equipment equipment = requireEquipment(id);
        // 已报废设备不再允许修改，避免历史资产状态被反复改写。
        if (equipment.getStatus() == EquipmentStatus.SCRAPPED) {
            throw new BusinessException("已报废设备不能修改");
        }
        fillEquipment(equipment, request);
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment scrap(Long id, AppUser operator) {
        if (!operator.getRole().canManageEquipment()) {
            throw new BusinessException("当前用户无权报废设备");
        }
        Equipment equipment = requireEquipment(id);
        // 报废动作交给状态对象处理，统一约束不同状态下的可执行操作。
        stateFactory.getState(equipment.getStatus()).scrap(equipment);
        Equipment saved = equipmentRepository.save(equipment);
        eventPublisher.publish(new DomainEvent(
                EventType.EQUIPMENT_SCRAPPED,
                operator,
                "设备报废登记",
                operator.getRealName() + " 将设备 " + saved.getName() + " 标记为报废"
        ));
        return saved;
    }

    private void fillEquipment(Equipment equipment, EquipmentCreateRequest request) {
        // 新增和修改共用同一套字段填充逻辑，避免重复代码。
        equipment.setCode(request.code());
        equipment.setName(request.name());
        equipment.setCategory(request.category());
        equipment.setLabRoom(request.labRoom());
        equipment.setValue(request.value());
        equipment.setManager(request.manager());
        equipment.setDescription(request.description());
    }
}
