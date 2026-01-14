package com.yebur.backendorderly.cashoperations;

import com.yebur.backendorderly.cashsessions.CashSession;
import com.yebur.backendorderly.cashsessions.CashSessionResponse;
import com.yebur.backendorderly.cashsessions.CashSessionService;
import com.yebur.backendorderly.orderdetail.OrderDetailRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("cashOperationService")
public class CashOperationService implements CashOperationServiceInterface{
    private final CashOperationRepository cashOperationRepository;
    private final CashSessionService cashSessionService;
    private final OrderDetailRepository orderDetailRepository;

    public CashOperationService(CashOperationRepository cashOperationRepository, CashSessionService cashSessionService, OrderDetailRepository orderDetailRepository) {
        this.cashOperationRepository = cashOperationRepository;
        this.cashSessionService = cashSessionService;
        this.orderDetailRepository = orderDetailRepository;
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
    public List<CashOperationResponse> findCashOperationDTOBySessionId(Long id){
        List<CashOperationResponse> operations = cashOperationRepository.findCashOperationBySessionId(id);
        BigDecimal paidCard = orderDetailRepository.getPaidSalesByCashSessionAndPaymentMethod(id, "CARD");
        BigDecimal paidCash = orderDetailRepository.getPaidSalesByCashSessionAndPaymentMethod(id, "CASH");
        System.out.println(paidCash + " " + paidCard);
        operations.add(0, new CashOperationResponse(null, id, CashOperationType.DEPOSIT, "CARD", "COBROS EN TARJETA", paidCard, null));
        operations.add(0,new CashOperationResponse(null, id, CashOperationType.DEPOSIT, "CASH" , "COBROS EN EFECTIVO", paidCash, null));
        return operations;
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
        toUpdate.setPaymentMethod(request.getPaymentMethod());
        toUpdate.setAmount(request.getAmount());
        return CashOperationResponse.mapToResponse(cashOperationRepository.save(toUpdate));
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
        cashOperation.setPaymentMethod(request.getPaymentMethod());
        cashOperation.setDescription(request.getDescription());
        cashOperation.setAmount(request.getAmount());

        return cashOperation;
    }
}
