package com.yebur.backendorderly.cashoperations;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/cashOperation")
public class CashOperationController {

    private final CashOperationService cashOperationService;

    public CashOperationController(CashOperationService cashOperationService) {
        this.cashOperationService = cashOperationService;
    }

    @GetMapping
    public ResponseEntity<List<CashOperationResponse>> findAllCashOperationDTO(){
        return ResponseEntity.ok(cashOperationService.findAllCashOperationDTO());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> findCashOperationDTOById(@PathVariable Long id){
        Optional<CashOperationResponse> optional = cashOperationService.findCashOperationDTOById(id);
        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        }else{
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "cash operation not found with this id: " + id));
        }
    }

    @GetMapping("/cashSession/id/{id}")
    public ResponseEntity<List<CashOperationResponse>> findCashOperaionDTOBYSessionId(@PathVariable Long id){
        return ResponseEntity.ok(cashOperationService.findCashOperationDTOBySessionId(id));
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateCashOperation(@PathVariable Long id, @RequestBody CashOperationRequest cashOperationRequest, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }
        try{
            return ResponseEntity.ok(cashOperationService.update(id, cashOperationRequest));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCashOperation(@Valid @RequestBody CashOperationRequest cashOperationRequest, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }

        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(cashOperationService.create(cashOperationRequest));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteCashOperation(@PathVariable Long id){
        try{
            cashOperationService.delete(id);
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