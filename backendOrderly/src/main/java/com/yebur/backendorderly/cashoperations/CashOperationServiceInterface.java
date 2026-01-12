package com.yebur.backendorderly.cashoperations;

import java.util.List;
import java.util.Optional;

public interface CashOperationServiceInterface {

    List<CashOperationResponse> findAllCashOperationDTO();

    Optional<CashOperation> findCashOperationById(Long id);

    Optional<CashOperationResponse> findCashOperationDTOById(Long id);

    List<CashOperationResponse> findCashOperationDTOBySessionId(Long id);

    CashOperationResponse create(CashOperationRequest request);

    CashOperationResponse update(Long id, CashOperationRequest request);

    void delete(Long id);
}
