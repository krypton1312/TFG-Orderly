package com.yebur.backendorderly.shiftrecord;

import com.yebur.backendorderly.employee.EmployeeResponse;
import com.yebur.backendorderly.employee.EmployeeServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shift-records")
@RequiredArgsConstructor
public class ShiftRecordController {

    private final ShiftRecordServiceInterface shiftRecordService;
    private final EmployeeServiceInterface employeeService;

    @GetMapping
    public ResponseEntity<List<ShiftRecordResponse>> getAll() {
        return ResponseEntity.ok(shiftRecordService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return shiftRecordService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftRecordResponse>> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(shiftRecordService.findByEmployeeId(employeeId));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ShiftRecordRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(shiftRecordService.create(request));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody ShiftRecordRequest request, BindingResult result, @PathVariable Long id, Authentication auth) {
        if (result.hasErrors()) {
            return validation(result);
        }
        try {
            // Get current employee ID to track who made the change
            Long editorId = employeeService.findCurrentEmployeeDTO(auth)
                    .map(EmployeeResponse::getId)
                    .orElseThrow(() -> new IllegalArgumentException("Current authenticated employee not found"));

            return ResponseEntity.ok(shiftRecordService.update(id, request, editorId));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            shiftRecordService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), "El campo: " + error.getField() + " " + error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
