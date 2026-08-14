package com.example.labdesign.repository;

import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.EquipmentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);

    List<Equipment> findByStatus(EquipmentStatus status);
}
