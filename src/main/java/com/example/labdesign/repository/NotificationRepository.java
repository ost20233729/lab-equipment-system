package com.example.labdesign.repository;

import com.example.labdesign.entity.AppUser;
import com.example.labdesign.entity.NotificationMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationMessage, Long> {
    List<NotificationMessage> findByRecipientOrderByCreatedAtDesc(AppUser recipient);
}
