package com.yebur.backendorderly.shiftrecord;

import com.yebur.backendorderly.employee.Employee;
import com.yebur.backendorderly.employee.EmployeeRepository;
import com.yebur.backendorderly.shiftrecordstory.ShiftRecordStory;
import com.yebur.backendorderly.shiftrecordstory.ShiftRecordStoryServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftRecordService implements ShiftRecordServiceInterface {

    private final ShiftRecordRepository shiftRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRecordStoryServiceInterface storyService;

    @Override
    @Transactional(readOnly = true)
    public List<ShiftRecordResponse> findAll() {
        return shiftRecordRepository.findAll().stream()
                .map(ShiftRecordResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftRecordResponse> findById(Long id) {
        return shiftRecordRepository.findById(id)
                .map(ShiftRecordResponse::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftRecordResponse> findByEmployeeId(Long employeeId) {
        return shiftRecordRepository.findByEmployeeId(employeeId).stream()
                .map(ShiftRecordResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShiftRecordResponse create(ShiftRecordRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + request.getEmployeeId()));

        ShiftRecord shiftRecord = new ShiftRecord();
        shiftRecord.setEmployee(employee);
        shiftRecord.setStartTime(request.getStartTime());
        shiftRecord.setEndTime(request.getEndTime());
        shiftRecord.setNotes(request.getNotes());

        ShiftRecord saved = shiftRecordRepository.save(shiftRecord);
        return ShiftRecordResponse.mapToResponse(saved);
    }

    @Override
    @Transactional
    public ShiftRecordResponse update(Long id, ShiftRecordRequest request, Long editorId) {
        ShiftRecord shiftRecord = shiftRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ShiftRecord not found with id: " + id));

        Employee editor = employeeRepository.findById(editorId)
                .orElseThrow(() -> new IllegalArgumentException("Editor (Employee) not found with id: " + editorId));

        // Create story record before updating
        ShiftRecordStory story = new ShiftRecordStory();
        story.setShiftRecord(shiftRecord);
        story.setOldEntryRecord(shiftRecord.getStartTime());
        story.setNewEntryRecord(request.getStartTime());
        story.setOldExitRecord(shiftRecord.getEndTime());
        story.setNewExitRecord(request.getEndTime());
        story.setNotes(request.getNotes());
        story.setModificationDate(LocalDateTime.now());
        story.setEmployee(editor);

        storyService.saveStory(story);

        // Update fields
        shiftRecord.setStartTime(request.getStartTime());
        shiftRecord.setEndTime(request.getEndTime());
        shiftRecord.setNotes(request.getNotes());

        ShiftRecord updated = shiftRecordRepository.save(shiftRecord);
        return ShiftRecordResponse.mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!shiftRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("ShiftRecord not found with id: " + id);
        }
        shiftRecordRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftRecordResponse> findOpenShiftByEmployeeId(Long employeeId) {
        return shiftRecordRepository.findFirstByEmployeeIdAndEndTimeIsNull(employeeId)
                .map(ShiftRecordResponse::mapToResponse);
    }

    @Override
    @Transactional
    public ShiftRecordResponse clockIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        shiftRecordRepository.findFirstByEmployeeIdAndEndTimeIsNull(employeeId).ifPresent(s -> {
            throw new IllegalStateException("Employee already has an open shift");
        });

        ShiftRecord shiftRecord = new ShiftRecord();
        shiftRecord.setEmployee(employee);
        shiftRecord.setStartTime(LocalDateTime.now());

        ShiftRecord saved = shiftRecordRepository.save(shiftRecord);
        return ShiftRecordResponse.mapToResponse(saved);
    }

    @Override
    @Transactional
    public ShiftRecordResponse clockOut(Long employeeId) {
        ShiftRecord openShift = shiftRecordRepository.findFirstByEmployeeIdAndEndTimeIsNull(employeeId)
                .orElseThrow(() -> new IllegalStateException("No open shift found for employee: " + employeeId));

        openShift.setEndTime(LocalDateTime.now());
        ShiftRecord saved = shiftRecordRepository.save(openShift);
        return ShiftRecordResponse.mapToResponse(saved);
    }
}
