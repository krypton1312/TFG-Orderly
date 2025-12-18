package com.yebur.backendorderly.cashoperations;

import com.yebur.backendorderly.cashsessions.CashSession;
import com.yebur.backendorderly.cashsessions.CashSessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("cashOperationService")
public class CashOperationService implements CashOperationServiceInterface{
    private final CashOperationRepository cashOperationRepository;
    private final CashSessionService cashSessionService;

    public CashOperationService(CashOperationRepository cashOperationRepository, CashSessionService cashSessionService) {
        this.cashOperationRepository = cashOperationRepository;
        this.cashSessionService = cashSessionService;
    }


    @Override
    public List<CashOperationResponse> findAllCashOperationDTO() {
        return cashOperationRepository.findAllCashOperationDTO();
    }

    @Override
    public Optional<CashOperation> findCashOperationById(Long id) {
        return cashOperationRepository.findCashOperationById(id);
    }

    @Override
    public Optional<CashOperationResponse> findCashOperationDTOById(Long id) {
        return cashOperationRepository.findCashOperationDTOById(id);
    }

    @Override
    public CashOperationResponse create(CashOperationRequest request) {
        CashOperation saved = cashOperationRepository.save(mapToEntity(request));
        return CashOperationResponse.mapToResponse(saved);
    }

    @Override
    public CashOperationResponse update(Long id, CashOperationRequest request) {
        CashOperation toUpdate = findCashOperationById(id).orElseThrow(() -> new IllegalStateException("CashOperation not found with this id" + id));
        CashSession session = cashSessionService.findCashSessionById(request.getSessionId()).orElseThrow(() -> new IllegalStateException("CashSession not found by this id:" + request.getSessionId()));


        toUpdate.setSession(session);
        toUpdate.setDescription(request.getDescription());
        toUpdate.setType(CashOperationType.valueOf(request.getType()));
        toUpdate.setAmount(request.getAmount());
        return null;
    }

    @Override
    public void delete(Long id) {
        CashOperation toDelete = findCashOperationById(id).orElseThrow(() -> new IllegalStateException("CashOperation not found with this id" + id));

        cashOperationRepository.delete(toDelete);
    }


    public CashOperation mapToEntity(CashOperationRequest request){
        CashOperation cashOperation = new CashOperation();
        CashSession session = cashSessionService.findCashSessionById(request.getSessionId()).orElseThrow(() -> new IllegalStateException("CashSession not found by this id:" + request.getSessionId()));

        cashOperation.setSession(session);
        cashOperation.setCreatedAt(LocalDateTime.now());
        cashOperation.setType(CashOperationType.valueOf(request.getType()));
        cashOperation.setDescription(request.getDescription());
        cashOperation.setAmount(request.getAmount());

        return cashOperation;
    }
}
