package com.yebur.backendorderly.cashcount;

import java.util.List;
import java.util.Optional;

public interface CashCountServiceInterface {
    Optional<CashCount> findById();

    Optional<CashCountResponse> findDTOById();

    Optional<CashCountResponse> findDTOBySessionId();

    List<CashCountResponse> findAllDTO();

    CashCountResponse create(CashCountRequest cashCountRequest);

    CashCountResponse update(CashCountRequest cashCountRequest);

    void delete(Long id);

}
