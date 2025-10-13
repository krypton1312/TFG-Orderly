package com.yebur.backendorderly.resttable;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RestTableRepository extends JpaRepository<RestTable, Long>{

    @Query("SELECT new com.yebur.backendorderly.resttable.RestTableResponse(rt.id, rt.number, rt.status) FROM RestTable rt")
    List<RestTableResponse> findAllRestTableDTO();

    @Override
    Optional<RestTable> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.resttable.RestTableResponse(rt.id, rt.number, rt.status) FROM RestTable rt WHERE rt.id= :id")
    Optional<RestTableResponse> findRestTableDTOById(Long id);

    @Query("SELECT new com.yebur.backendorderly.resttable.RestTableResponse(rt.id, rt.number, rt.status) FROM RestTable rt WHERE rt.number= :number")
    Optional<RestTableResponse> findRestTableDTOByNumber(int number);
}
