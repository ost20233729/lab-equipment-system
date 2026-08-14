package com.example.labdesign.pattern.approval;

import com.example.labdesign.entity.Equipment;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ApprovalContext {
    private final Equipment equipment;
    private final LocalDate startDate;
    private final LocalDate expectedReturnDate;

    public ApprovalContext(Equipment equipment, LocalDate startDate, LocalDate expectedReturnDate) {
        this.equipment = equipment;
        this.startDate = startDate;
        this.expectedReturnDate = expectedReturnDate;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public long getBorrowDays() {
        return ChronoUnit.DAYS.between(startDate, expectedReturnDate) + 1;
    }
}
