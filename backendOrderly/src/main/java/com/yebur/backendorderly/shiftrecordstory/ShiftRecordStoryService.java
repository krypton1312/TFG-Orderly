package com.yebur.backendorderly.shiftrecordstory;

import com.yebur.backendorderly.employee.Employee;
import com.yebur.backendorderly.employee.EmployeeRepository;
import com.yebur.backendorderly.shiftrecord.ShiftRecord;
import com.yebur.backendorderly.shiftrecord.ShiftRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftRecordStoryService implements ShiftRecordStoryServiceInterface {

    private final ShiftRecordStoryRepository repository;
    private final ShiftRecordRepository shiftRecordRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShiftRecordStoryResponse> findAll() {
        return repository.findAll().stream()
                .map(ShiftRecordStoryResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftRecordStoryResponse> findById(Long id) {
        return repository.findById(id).map(ShiftRecordStoryResponse::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftRecordStoryResponse> findByShiftRecordId(Long shiftRecordId) {
        return repository.findByShiftRecordId(shiftRecordId).stream()
                .map(ShiftRecordStoryResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShiftRecordStoryResponse create(ShiftRecordStoryRequest request) {
        ShiftRecord shiftRecord = shiftRecordRepository.findById(request.getShiftRecordId())
                .orElseThrow(() -> new IllegalArgumentException("ShiftRecord not found with id: " + request.getShiftRecordId()));
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + request.getEmployeeId()));

        ShiftRecordStory story = new ShiftRecordStory();
        story.setShiftRecord(shiftRecord);
        story.setOldEntryRecord(request.getOldEntryRecord());
        story.setNewEntryRecord(request.getNewEntryRecord());
        story.setOldExitRecord(request.getOldExitRecord());
        story.setNewExitRecord(request.getNewExitRecord());
        story.setNotes(request.getNotes());
        story.setModificationDate(LocalDateTime.now());
        story.setEmployee(employee);

        return ShiftRecordStoryResponse.mapToResponse(repository.save(story));
    }

    @Override
    @Transactional
    public ShiftRecordStoryResponse update(Long id, ShiftRecordStoryRequest request) {
        ShiftRecordStory story = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ShiftRecordStory not found with id: " + id));

        if (request.getShiftRecordId() != null && (story.getShiftRecord() == null || !story.getShiftRecord().getId().equals(request.getShiftRecordId()))) {
            ShiftRecord shiftRecord = shiftRecordRepository.findById(request.getShiftRecordId())
                    .orElseThrow(() -> new IllegalArgumentException("ShiftRecord not found with id: " + request.getShiftRecordId()));
            story.setShiftRecord(shiftRecord);
        }

        if (request.getEmployeeId() != null && (story.getEmployee() == null || !story.getEmployee().getId().equals(request.getEmployeeId()))) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + request.getEmployeeId()));
            story.setEmployee(employee);
        }

        story.setOldEntryRecord(request.getOldEntryRecord());
        story.setNewEntryRecord(request.getNewEntryRecord());
        story.setOldExitRecord(request.getOldExitRecord());
        story.setNewExitRecord(request.getNewExitRecord());
        story.setNotes(request.getNotes());
        story.setModificationDate(LocalDateTime.now());

        return ShiftRecordStoryResponse.mapToResponse(repository.save(story));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("ShiftRecordStory not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void saveStory(ShiftRecordStory story) {
        repository.save(story);
    }
}
