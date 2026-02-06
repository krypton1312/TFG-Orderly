package com.yebur.backendorderly.shiftrecordstory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftRecordStoryResponse {
    private Long id;
    private Long shiftRecordId;
    private LocalDateTime oldEntryRecord;
    private LocalDateTime newEntryRecord;
    private LocalDateTime oldExitRecord;
    private LocalDateTime newExitRecord;
    private String notes;
    private LocalDateTime modificationDate;
    private Long employeeId; // Тот кто изменил

    public static ShiftRecordStoryResponse mapToResponse(ShiftRecordStory story) {
        return ShiftRecordStoryResponse.builder()
                .id(story.getId())
                .shiftRecordId(story.getShiftRecord().getId())
                .oldEntryRecord(story.getOldEntryRecord())
                .newEntryRecord(story.getNewEntryRecord())
                .oldExitRecord(story.getOldExitRecord())
                .newExitRecord(story.getNewExitRecord())
                .notes(story.getNotes())
                .modificationDate(story.getModificationDate())
                .employeeId(story.getEmployee().getId())
                .build();
    }
}
