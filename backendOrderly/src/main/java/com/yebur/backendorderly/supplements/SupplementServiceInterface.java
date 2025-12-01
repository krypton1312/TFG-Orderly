package com.yebur.backendorderly.supplements;

import java.util.List;
import java.util.Optional;

public interface SupplementServiceInterface {

    Optional<Supplement> findSupplementById(Long id);

    Optional<SupplementResponse> findSupplementDTOById(Long id);

    List<SupplementResponse> findAllSupplementsDTO();

    List<SupplementResponse> findSupplementsByCategory(Long id);

    SupplementResponse createSupplement(SupplementRequest supplement);

    SupplementResponse updateSupplement(Long id, SupplementRequest supplement);

    void deleteSupplement(Long id);
}
