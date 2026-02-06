package com.yebur.backendorderly.shiftrecordstory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftRecordStoryRequest {
    @NotNull
    private Long shiftRecordId;

    private LocalDateTime oldEntryRecord;
    private LocalDateTime newEntryRecord;
    private LocalDateTime oldExitRecord;
    private LocalDateTime newExitRecord;

    private String notes;

    @NotNull
    private Long employeeId;
}
