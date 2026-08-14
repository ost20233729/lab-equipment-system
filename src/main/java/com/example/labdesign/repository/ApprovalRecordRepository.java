package com.example.labdesign.repository;

import com.example.labdesign.entity.ApprovalRecord;
import com.example.labdesign.entity.BorrowRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {
    List<ApprovalRecord> findByBorrowRequestOrderByCreatedAtAsc(BorrowRequest borrowRequest);
}
