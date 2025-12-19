package com.yebur.backendorderly.cashcount;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/cashcounts")
public class CashCountController {

    private final CashCountService cashCountService;

    public CashCountController(CashCountService cashCountService) {
        this.cashCountService = cashCountService;
    }

    @GetMapping
    public ResponseEntity<List<CashCountResponse>> findAllCashCountsDTO(){
        return ResponseEntity.ok(cashCountService.findAllCashCountDTO());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> findCashCountDTOById(@PathVariable Long id){
        Optional<CashCountResponse> optional = cashCountService.findCashCountDTOById(id);
        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        }else{
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "cash count not found with this id: " + id));
        }
    }

    @GetMapping("/sessionId/{id}")
    public ResponseEntity<?> findCashCountDTOBySessionId(@PathVariable Long id){
        Optional<CashCountResponse> optional = cashCountService.findCashCountDTOBySessionId(id);
        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        }else{
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "no cash count found by this session id:" + id));
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateCashCount(@PathVariable Long id, @RequestBody CashCountRequest cashCountRequest, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }
        try{
            return ResponseEntity.ok(cashCountService.update(id, cashCountRequest));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCashCount(@Valid @RequestBody CashCountRequest cashCountRequest, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }

        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(cashCountService.create(cashCountRequest));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteCashCount(@PathVariable Long id){
        try{
            cashCountService.delete(id);
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
