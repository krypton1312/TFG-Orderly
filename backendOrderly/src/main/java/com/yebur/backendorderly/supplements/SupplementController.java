package com.yebur.backendorderly.supplements;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/supplements")
public class SupplementController {

    private final SupplementService supplementService;

    public SupplementController(SupplementService supplementService) {
        this.supplementService = supplementService;
    }

    @GetMapping
    public ResponseEntity<List<SupplementResponse>> findAllSupplementsDTO() {
        return ResponseEntity.ok(supplementService.findAllSupplementsDTO());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> findSupplementDTOById(@PathVariable Long id){
        Optional<SupplementResponse> optional = supplementService.findSupplementDTOById(id);
        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        }else{
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "supplement not found with this id:" + id));
        }
    }

    @GetMapping("/category/id")
    public ResponseEntity<List<?>> findAllSupplementsByCategory(@PathVariable Long id){
        List<SupplementResponse> optionals = supplementService.findSupplementsByCategory(id);
        if(!optionals.isEmpty()){
            return ResponseEntity.ok(optionals);
        }else{
            return ResponseEntity.status(404).body(Collections.singletonList("error"));
        }
    }

    @PutMapping("/id/id")
    public ResponseEntity<?> updateSupplementDTO(@PathVariable Long id, @RequestBody SupplementRequest supplementRequest, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }
        try{
            SupplementResponse updated = supplementService.updateSupplement(id, supplementRequest);
            return ResponseEntity.ok(updated);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createSupplement(@Valid @RequestBody SupplementRequest supplementRequest, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }

        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(supplementService.createSupplement(supplementRequest));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteSupplement(@PathVariable Long id){
        try{
            supplementService.deleteSupplement(id);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
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
