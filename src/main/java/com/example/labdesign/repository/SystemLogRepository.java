package com.example.labdesign.repository;

import com.example.labdesign.entity.SystemLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findTop50ByOrderByCreatedAtDesc();
}
