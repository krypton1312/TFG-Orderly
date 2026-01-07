package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCount;
import com.yebur.backendorderly.cashcount.CashCountRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CashSessionServiceInterface {
    Optional<CashSession> findCashSessionById(Long id);

    List<CashSessionResponse> findAllCashSessionDTO();

    Optional<CashSessionResponse> findCashSessionDTOById(Long id);

    Optional<CashSessionResponse> findCashSessionDTOByBusinessDate(LocalDate businessDate);

    CashSessionResponse create(CashSessionRequest request);

    CashSessionResponse open();

    CashSessionResponse close(Long id, CashCount cashCount);

    CashSessionResponse update(Long id, CashSessionRequest request);

    boolean existsCashSessionByStatus(CashSessionStatus status);

    Optional<CashSessionResponse> findCashSessionDTOByStatus(CashSessionStatus status);

    void delete(Long id);
}
