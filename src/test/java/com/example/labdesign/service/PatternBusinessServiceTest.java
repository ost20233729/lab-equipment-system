package com.example.labdesign.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.labdesign.dto.BorrowCreateRequest;
import com.example.labdesign.dto.RepairCompleteRequest;
import com.example.labdesign.dto.RepairCreateRequest;
import com.example.labdesign.dto.ReturnRequest;
import com.example.labdesign.entity.AppUser;
import com.example.labdesign.entity.BorrowRequest;
import com.example.labdesign.entity.Equipment;
import com.example.labdesign.entity.RepairTicket;
import com.example.labdesign.enums.BorrowStatus;
import com.example.labdesign.enums.EquipmentCategory;
import com.example.labdesign.enums.EquipmentStatus;
import com.example.labdesign.enums.RepairStatus;
import com.example.labdesign.enums.UserRole;
import com.example.labdesign.repository.AppUserRepository;
import com.example.labdesign.repository.EquipmentRepository;
import com.example.labdesign.repository.NotificationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PatternBusinessServiceTest {
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private RepairService repairService;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private AppUser student;
    private AppUser labAdmin;
    private AppUser teacher;
    private AppUser dean;

    @BeforeEach
    void setUp() {
        student = appUserRepository.findByUsername("student01").orElseThrow();
        labAdmin = appUserRepository.findByUsername("labadmin").orElseThrow();
        teacher = appUserRepository.findByUsername("teacher").orElseThrow();
        dean = appUserRepository.findByUsername("dean").orElseThrow();
    }

    @Test
    void normalEquipmentShortBorrowCanBeApprovedByLabAdmin() {
        Equipment normal = equipment("T-N-001", EquipmentCategory.NORMAL, "1000.00");
        BorrowRequest request = borrowService.create(new BorrowCreateRequest(
                normal.getId(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "普通实验"
        ), student);

        assertThat(request.getRequiredApproverRole()).isEqualTo(UserRole.LAB_ADMIN);

        BorrowRequest approved = borrowService.approve(request.getId(), "同意", labAdmin);

        assertThat(approved.getStatus()).isEqualTo(BorrowStatus.APPROVED);
        assertThat(equipmentRepository.findById(normal.getId()).orElseThrow().getStatus()).isEqualTo(EquipmentStatus.BORROWED);
        assertThat(notificationRepository.findByRecipientOrderByCreatedAtDesc(student)).isNotEmpty();
    }

    @Test
    void precisionEquipmentRequiresDeanApproval() {
        Equipment precision = equipment("T-P-001", EquipmentCategory.PRECISION, "50000.00");
        BorrowRequest request = borrowService.create(new BorrowCreateRequest(
                precision.getId(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "精密测量"
        ), student);

        assertThat(request.getRequiredApproverRole()).isEqualTo(UserRole.DEAN);
        assertThatThrownBy(() -> borrowService.approve(request.getId(), "越权审批", teacher))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权审批");

        BorrowRequest approved = borrowService.approve(request.getId(), "学院负责人审批通过", dean);
        assertThat(approved.getStatus()).isEqualTo(BorrowStatus.APPROVED);
    }

    @Test
    void returnUsesFeeStrategyByCategory() {
        Equipment computer = equipment("T-C-001", EquipmentCategory.COMPUTER, "12000.00");
        BorrowRequest request = borrowService.create(new BorrowCreateRequest(
                computer.getId(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "模型训练"
        ), student);
        BorrowRequest approved = borrowService.approve(request.getId(), "同意", teacher);

        BorrowRequest returned = borrowService.returnEquipment(approved.getId(),
                new ReturnRequest(LocalDate.now().plusDays(5)), labAdmin);

        assertThat(returned.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(returned.getOverdueFee()).isEqualByComparingTo("10.00");
        assertThat(equipmentRepository.findById(computer.getId()).orElseThrow().getStatus()).isEqualTo(EquipmentStatus.AVAILABLE);
    }

    @Test
    void repairFlowChangesEquipmentState() {
        Equipment equipment = equipment("T-R-001", EquipmentCategory.NORMAL, "800.00");

        RepairTicket ticket = repairService.create(new RepairCreateRequest(equipment.getId(), "无法开机"), student);
        assertThat(ticket.getStatus()).isEqualTo(RepairStatus.PENDING);
        assertThat(equipmentRepository.findById(equipment.getId()).orElseThrow().getStatus()).isEqualTo(EquipmentStatus.REPAIRING);

        repairService.start(ticket.getId(), labAdmin);
        RepairTicket completed = repairService.complete(ticket.getId(), new RepairCompleteRequest("更换保险丝后正常"), labAdmin);

        assertThat(completed.getStatus()).isEqualTo(RepairStatus.COMPLETED);
        assertThat(equipmentRepository.findById(equipment.getId()).orElseThrow().getStatus()).isEqualTo(EquipmentStatus.AVAILABLE);
    }

    private Equipment equipment(String code, EquipmentCategory category, String value) {
        Equipment equipment = new Equipment();
        equipment.setCode(code);
        equipment.setName(code + "测试设备");
        equipment.setCategory(category);
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipment.setLabRoom("测试实验室");
        equipment.setValue(new BigDecimal(value));
        equipment.setManager("测试管理员");
        equipment.setDescription("测试数据");
        return equipmentRepository.save(equipment);
    }
}
