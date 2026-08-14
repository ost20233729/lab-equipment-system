package com.example.labdesign.repository;

import com.example.labdesign.entity.AppUser;
import com.example.labdesign.entity.BorrowRequest;
import com.example.labdesign.entity.Equipment;
import com.example.labdesign.enums.BorrowStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    @Override
    @EntityGraph(attributePaths = {"equipment", "applicant", "approver"})
    Optional<BorrowRequest> findById(Long id);

    @EntityGraph(attributePaths = {"equipment", "applicant", "approver"})
    List<BorrowRequest> findByApplicantOrderByCreatedAtDesc(AppUser applicant);

    @EntityGraph(attributePaths = {"equipment", "applicant", "approver"})
    List<BorrowRequest> findByStatusOrderByCreatedAtDesc(BorrowStatus status);

    @EntityGraph(attributePaths = {"equipment", "applicant", "approver"})
    List<BorrowRequest> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"equipment", "applicant", "approver"})
    Optional<BorrowRequest> findFirstByEquipmentAndStatus(Equipment equipment, BorrowStatus status);
}
