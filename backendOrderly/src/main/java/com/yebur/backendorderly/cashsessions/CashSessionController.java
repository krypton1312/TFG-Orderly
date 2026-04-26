package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCount;
import com.yebur.backendorderly.cashcount.CashCountRequest;
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

    @PostMapping("/{id}/close")
    public ResponseEntity<?> closeCashSession(
            @PathVariable Long id,
            @RequestBody CashCountRequest cashCountRequest) {
        try {
            CashCount cashCount = toCashCountEntity(cashCountRequest);
            return ResponseEntity.ok(cashSessionService.close(id, cashCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", e.getMessage()));
        }
    }

    private CashCount toCashCountEntity(CashCountRequest req) {
        CashCount cc = new CashCount();
        cc.setC001(req.getC001());
        cc.setC002(req.getC002());
        cc.setC005(req.getC005());
        cc.setC010(req.getC010());
        cc.setC020(req.getC020());
        cc.setC050(req.getC050());
        cc.setC100(req.getC100());
        cc.setC200(req.getC200());
        cc.setB005(req.getB005());
        cc.setB010(req.getB010());
        cc.setB020(req.getB020());
        cc.setB050(req.getB050());
        cc.setB100(req.getB100());
        cc.setB200(req.getB200());
        cc.setB500(req.getB500());
        return cc;
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
