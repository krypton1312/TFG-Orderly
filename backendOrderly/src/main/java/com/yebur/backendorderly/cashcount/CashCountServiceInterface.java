package com.yebur.backendorderly.cashcount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CashCountServiceInterface {
    Optional<CashCount> findCashCountById(Long id);

    Optional<CashCountResponse> findCashCountDTOById(Long id);

    Optional<CashCountResponse> findCashCountDTOBySessionId(Long sessionId);

    List<CashCountResponse> findAllCashCountDTO();

    CashCountResponse create(CashCountRequest cashCountRequest);

    CashCountResponse update(Long id, CashCountRequest cashCountRequest);

    BigDecimal getLastCashCountTotal();

    void delete(Long id);

}
