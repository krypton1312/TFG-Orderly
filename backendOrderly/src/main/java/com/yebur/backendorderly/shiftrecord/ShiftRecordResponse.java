package com.yebur.backendorderly.shiftrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftRecordResponse {
    private Long id;
    private Long employeeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;

    public static ShiftRecordResponse mapToResponse(ShiftRecord shiftRecord) {
        return ShiftRecordResponse.builder()
                .id(shiftRecord.getId())
                .employeeId(shiftRecord.getEmployee().getId())
                .startTime(shiftRecord.getStartTime())
                .endTime(shiftRecord.getEndTime())
                .notes(shiftRecord.getNotes())
                .build();
    }
}
