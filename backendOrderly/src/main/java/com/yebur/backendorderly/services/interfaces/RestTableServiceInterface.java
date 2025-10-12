package com.yebur.backendorderly.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.yebur.backendorderly.dto.input.RestTableRequest;
import com.yebur.backendorderly.dto.output.RestTableResponse;
import com.yebur.backendorderly.entities.RestTable;

public interface RestTableServiceInterface {
    
    List<RestTableResponse> findAllRestTableDTO();

    Optional<RestTable> findById(Long id);

    Optional<RestTableResponse> findRestTableDTOById(Long id);

    Optional<RestTableResponse> findRestTableDTOByNumber(int number);

    RestTableResponse createRestTable(RestTableRequest restTable);

    RestTableResponse updateRestTable(Long id,RestTableRequest restTable);

    void deleteRestTable(Long id);
}
