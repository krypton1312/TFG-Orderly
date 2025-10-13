package com.yebur.backendorderly.resttable;

import java.util.List;
import java.util.Optional;

public interface RestTableServiceInterface {
    
    List<RestTableResponse> findAllRestTableDTO();

    Optional<RestTable> findById(Long id);

    Optional<RestTableResponse> findRestTableDTOById(Long id);

    Optional<RestTableResponse> findRestTableDTOByNumber(int number);

    RestTableResponse createRestTable(RestTableRequest restTable);

    RestTableResponse updateRestTable(Long id,RestTableRequest restTable);

    void deleteRestTable(Long id);
}
