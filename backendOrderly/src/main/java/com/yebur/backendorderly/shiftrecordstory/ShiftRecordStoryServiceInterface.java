package com.yebur.backendorderly.shiftrecordstory;

import java.util.List;
import java.util.Optional;

public interface ShiftRecordStoryServiceInterface {
    List<ShiftRecordStoryResponse> findAll();
    Optional<ShiftRecordStoryResponse> findById(Long id);
    List<ShiftRecordStoryResponse> findByShiftRecordId(Long shiftRecordId);
    ShiftRecordStoryResponse create(ShiftRecordStoryRequest request);
    ShiftRecordStoryResponse update(Long id, ShiftRecordStoryRequest request);
    void delete(Long id);

    // Вспомогательный метод для автоматической записи истории из других сервисов
    void saveStory(ShiftRecordStory story);
}
