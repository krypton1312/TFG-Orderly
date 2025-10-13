package com.yebur.backendorderly.resttable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tables")
public class RestTableController {
    private final RestTableService restTableService;

    public RestTableController(RestTableService restTableService) {
        this.restTableService = restTableService;
    }

    @GetMapping
    public ResponseEntity<List<RestTableResponse>> getAllTables() {
        return ResponseEntity.ok(restTableService.findAllRestTableDTO());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getTableById(@PathVariable Long id) {
        Optional<RestTableResponse> optional = restTableService.findRestTableDTOById(id);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.orElseThrow());
        } else {
            return ResponseEntity.status(404)
                    .body(Collections.singletonMap("error", "table not found with this id:" + id));
        }
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<?> getTableByNumber(@PathVariable int number) {
        Optional<RestTableResponse> optional = restTableService.findRestTableDTOByNumber(number);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.orElseThrow());
        } else {
            return ResponseEntity.status(404)
                    .body(Collections.singletonMap("error", "table not found with this number:" + number));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTable(@RequestBody RestTableRequest table, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(restTableService.createRestTable(table));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateTable(@PathVariable Long id, @RequestBody RestTableRequest table,
            BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }

        try {
            RestTableResponse updated = restTableService.updateRestTable(id, table);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteTable(@PathVariable Long id) {
        try {
            restTableService.deleteRestTable(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
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
