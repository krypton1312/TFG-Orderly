package com.yebur.backendorderly.shiftrecordstory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRecordStoryRepository extends JpaRepository<ShiftRecordStory, Long> {
    List<ShiftRecordStory> findByShiftRecordId(Long shiftRecordId);
}
