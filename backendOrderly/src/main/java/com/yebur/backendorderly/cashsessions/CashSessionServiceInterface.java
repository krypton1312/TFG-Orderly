package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCountRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CashSessionServiceInterface {
    Optional<CashSession> findById(Long id);

    List<CashSessionResponse> findAllDTO();

    Optional<CashSessionResponse> findDTOById(Long id);

    CashSessionResponse create(CashCountRequest request);

    CashSessionResponse open(BigDecimal cashStart);

    CashSessionResponse close(Long id, BigDecimal cashEndActual);

    CashSessionResponse update(CashCountRequest request);

    void delete(Long id);
}
