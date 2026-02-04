package com.yebur.backendorderly.employee;

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
import java.util.Optional;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeServiceInterface employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.findAllEmployeesDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        Optional<EmployeeResponse> employee = employeeService.findEmployeeDTOById(id);
        if (employee.isPresent()) {
            return ResponseEntity.ok(employee.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Employee not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getEmployeeByEmail(@PathVariable String email) {
        Optional<EmployeeResponse> employee = employeeService.findEmployeeDTOByEmail(email);
        if (employee.isPresent()) {
            return ResponseEntity.ok(employee.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Employee not found with email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/currentEmployee")
    public ResponseEntity<?> getCurrentEmployee(Authentication auth) {
        Optional<EmployeeResponse> employee = employeeService.findCurrentEmployeeDTO(auth);
        if (employee.isPresent()) {
            return ResponseEntity.ok(employee.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Employee not found with email: " + auth);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EmployeeRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody EmployeeRequest request, BindingResult result, @PathVariable Long id) {
        if (result.hasErrors()) {
            return validation(result);
        }
        try {
            return ResponseEntity.ok(employeeService.update(id, request));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            employeeService.delete(id);
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
