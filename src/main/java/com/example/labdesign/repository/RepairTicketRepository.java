package com.example.labdesign.repository;

import com.example.labdesign.entity.RepairTicket;
import com.example.labdesign.enums.RepairStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairTicketRepository extends JpaRepository<RepairTicket, Long> {
    @Override
    @EntityGraph(attributePaths = {"equipment", "reporter", "handler"})
    Optional<RepairTicket> findById(Long id);

    @EntityGraph(attributePaths = {"equipment", "reporter", "handler"})
    List<RepairTicket> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"equipment", "reporter", "handler"})
    List<RepairTicket> findByStatusOrderByCreatedAtDesc(RepairStatus status);
}
