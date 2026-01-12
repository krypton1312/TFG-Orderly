package com.yebur.backendorderly.cashoperations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {

    @Query("SELECT new com.yebur.backendorderly.cashoperations.CashOperationResponse(co.id, co.session.id, co.type, co.description, co.amount, co.createdAt) FROM CashOperation co")
    List<CashOperationResponse> findAllCashOperationDTO();

    @Query("SELECT new com.yebur.backendorderly.cashoperations.CashOperationResponse(co.id, co.session.id, co.type, co.description, co.amount, co.createdAt) FROM CashOperation co WHERE co.id = :id")
    Optional<CashOperationResponse> findCashOperationDTOById(Long id);

    @Query("SELECT new com.yebur.backendorderly.cashoperations.CashOperationResponse(co.id, co.session.id, co.type, co.description, co.amount, co.createdAt) FROM CashOperation co WHERE co.session.id = :id")
    List<CashOperationResponse> findCashOperationBySessionId(Long id);

    Optional<CashOperation> findCashOperationById(Long id);

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN co.type = 'DEPOSIT' THEN co.amount
                WHEN co.type = 'WITHDRAW' THEN -co.amount
                ELSE 0
            END), 0) FROM CashOperation co WHERE co.session.id = :cashSessionId
            """)
    BigDecimal getNetAmountByCashSession(Long cashSessionId);
}
