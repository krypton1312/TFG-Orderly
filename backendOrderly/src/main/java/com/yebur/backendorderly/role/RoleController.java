package com.yebur.backendorderly.role;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleServiceInterface roleService;

    public RoleController(RoleServiceInterface roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAllRoleDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        Optional<RoleResponse> role = roleService.findRoleDTOById(id);
        if (role.isPresent()) {
            return ResponseEntity.ok(role.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Role not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RoleRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody RoleRequest request, BindingResult result, @PathVariable Long id) {
        if (result.hasErrors()) {
            return validation(result);
        }
        try {
            return ResponseEntity.ok(roleService.update(id, request));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            roleService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
