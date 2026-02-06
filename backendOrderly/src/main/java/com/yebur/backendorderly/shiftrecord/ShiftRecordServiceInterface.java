package com.yebur.backendorderly.shiftrecord;

import java.util.List;
import java.util.Optional;

public interface ShiftRecordServiceInterface {
    List<ShiftRecordResponse> findAll();
    Optional<ShiftRecordResponse> findById(Long id);
    List<ShiftRecordResponse> findByEmployeeId(Long employeeId);
    ShiftRecordResponse create(ShiftRecordRequest request);
    ShiftRecordResponse update(Long id, ShiftRecordRequest request, Long editorId);
    void delete(Long id);
}
