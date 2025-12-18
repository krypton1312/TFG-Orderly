package com.yebur.backendorderly.cashoperations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {

    @Query("SELECT new com.yebur.backendorderly.cashoperations.CashOperationResponse(co.id, co.session.id, co.type, co.description, co.amount, co.createdAt) FROM CashOperation co")
    List<CashOperationResponse> findAllCashOperationDTO();

    @Query("SELECT new com.yebur.backendorderly.cashoperations.CashOperationResponse(co.id, co.session.id, co.type, co.description, co.amount, co.createdAt) FROM CashOperation co WHERE co.id = :id")
    Optional<CashOperationResponse> findCashOperationDTOById(Long id);

    Optional<CashOperation> findCashOperationById(Long id);

    List<CashOperation> id(Long id);

    void deleteCashOperationById(Long id);
}
