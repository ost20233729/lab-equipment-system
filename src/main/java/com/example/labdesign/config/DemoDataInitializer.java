package com.example.labdesign.config;

import com.example.labdesign.entity.AppUser;
import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.EquipmentCategory;
import com.example.labdesign.enums.EquipmentStatus;
import com.example.labdesign.enums.UserRole;
import com.example.labdesign.repository.AppUserRepository;
import com.example.labdesign.repository.EquipmentRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 在空库启动时注入一组最小可演示数据，方便课堂展示和联调。
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {
    private final AppUserRepository appUserRepository;
    private final EquipmentRepository equipmentRepository;

    public DemoDataInitializer(AppUserRepository appUserRepository, EquipmentRepository equipmentRepository) {
        this.appUserRepository = appUserRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public void run(String... args) {
        // 仅在用户表为空时初始化，避免重复启动造成重复数据。
        if (appUserRepository.count() == 0) {
            appUserRepository.saveAll(List.of(
                    user("student01", "欧书团", UserRole.STUDENT, "软件工程2023级"),
                    user("labadmin", "王老师", UserRole.LAB_ADMIN, "人工智能实验室"),
                    user("teacher", "李教授", UserRole.TEACHER, "计算机与数学学院"),
                    user("dean", "张院长", UserRole.DEAN, "计算机与数学学院")
            ));
        }
        // 初始化不同类别和价值区间的设备，便于演示审批链与费用策略。
        if (equipmentRepository.count() == 0) {
            equipmentRepository.saveAll(List.of(
                    equipment("LAB-N-001", "数字万用表", EquipmentCategory.NORMAL, "综合实验室A301", "680.00", "王老师", "常规电路实验测量设备"),
                    equipment("LAB-C-002", "深度学习工作站", EquipmentCategory.COMPUTER, "人工智能实验室B204", "15800.00", "李教授", "GPU计算与课程设计实验设备"),
                    equipment("LAB-P-003", "高精度示波器", EquipmentCategory.PRECISION, "电子技术实验室C102", "42600.00", "张院长", "高价值精密仪器，需高级审批"),
                    equipment("LAB-N-004", "Arduino 传感器套件", EquipmentCategory.NORMAL, "创新实验室D201", "360.00", "王老师", "创新训练和课程实验套件")
            ));
        }
    }

    private AppUser user(String username, String realName, UserRole role, String department) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setRealName(realName);
        user.setRole(role);
        user.setDepartment(department);
        return user;
    }

    private Equipment equipment(String code, String name, EquipmentCategory category, String labRoom,
                                String value, String manager, String description) {
        Equipment equipment = new Equipment();
        equipment.setCode(code);
        equipment.setName(name);
        equipment.setCategory(category);
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipment.setLabRoom(labRoom);
        equipment.setValue(new BigDecimal(value));
        equipment.setManager(manager);
        equipment.setDescription(description);
        return equipment;
    }
}
