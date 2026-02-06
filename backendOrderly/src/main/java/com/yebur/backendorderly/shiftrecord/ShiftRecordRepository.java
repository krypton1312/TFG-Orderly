package com.yebur.backendorderly.shiftrecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRecordRepository extends JpaRepository<ShiftRecord, Long> {
    List<ShiftRecord> findByEmployeeId(Long employeeId);
}
