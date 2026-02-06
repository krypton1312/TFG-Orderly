package com.yebur.backendorderly.shiftrecord;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftRecordRequest {
    @NotNull
    private Long employeeId;
    
    @NotNull
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String notes;
}
