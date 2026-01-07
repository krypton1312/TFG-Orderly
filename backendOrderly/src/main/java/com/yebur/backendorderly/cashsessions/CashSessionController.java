package com.yebur.backendorderly.cashsessions;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/cashSession")
public class CashSessionController {

    private final CashSessionService cashSessionService;

    public CashSessionController(CashSessionService cashSessionService) {
        this.cashSessionService = cashSessionService;
    }


    @GetMapping
    public ResponseEntity<List<CashSessionResponse>> findAllCashSessionDTO(){
        return ResponseEntity.ok(cashSessionService.findAllCashSessionDTO());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> findCashSessionDTOById(@PathVariable Long id){
        Optional<CashSessionResponse> optional = cashSessionService.findCashSessionDTOById(id);
        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        }else{
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "cash session not found by this id: "+  id));
        }
    }

    @GetMapping("/businessDate/{date}")
    public ResponseEntity<?> findCashSessionDTOByBusinessDate(@PathVariable LocalDate date){
        Optional<CashSessionResponse> optional = cashSessionService.findCashSessionDTOByBusinessDate(date);

        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        }else{
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "no cash session found by this business date: " + date.toString()));
        }
    }

    @GetMapping("/existsByStatus/{status}")
    public ResponseEntity<?> existsByStatus(@PathVariable CashSessionStatus status){
        return ResponseEntity.ok(cashSessionService.existsCashSessionByStatus(status));
    }

    @GetMapping("/byStatus/{status}")
    public ResponseEntity<?> findCashSessionDTOByStatus(@PathVariable CashSessionStatus status){
        Optional<CashSessionResponse> optional = cashSessionService.findCashSessionDTOByStatus(status);

        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        }else{
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "there is no cash session by this status: " + status.toString()));
        }
    }

    @PostMapping("/open")
    public ResponseEntity<?> openCashSession(){
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(cashSessionService.open());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateCashSession(@PathVariable Long id, @RequestBody CashSessionRequest cashSession, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }
        try{
            return ResponseEntity.ok(cashSessionService.update(id, cashSession));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteCashSession(@PathVariable Long id){
        try{
            cashSessionService.delete(id);
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
