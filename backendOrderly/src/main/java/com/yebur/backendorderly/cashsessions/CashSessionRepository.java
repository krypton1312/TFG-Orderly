package com.yebur.backendorderly.cashsessions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashSessionRepository extends JpaRepository<CashSession, Long> {

    @Query("SELECT new com.yebur.backendorderly.cashsessions.CashSessionResponse(cs.id, cs.businessDate, cs.shiftNo, cs.openedAt, cs.closedAt, cs.cashStart, cs.cashEndExpected, cs.cashEndActual, cs.difference, cs.totalSalesCash, cs.totalSalesCard, cs.status) FROM CashSession cs")
    List<CashSessionResponse> findAllCashSessionDTO();

    @Query("SELECT new com.yebur.backendorderly.cashsessions.CashSessionResponse(cs.id, cs.businessDate, cs.shiftNo, cs.openedAt, cs.closedAt, cs.cashStart, cs.cashEndExpected, cs.cashEndActual, cs.difference, cs.totalSalesCash, cs.totalSalesCard, cs.status) FROM CashSession cs WHERE cs.id = :id")
    Optional<CashSessionResponse> findCashSessionDTOById(Long id);

    @Query("SELECT new com.yebur.backendorderly.cashsessions.CashSessionResponse(cs.id, cs.businessDate, cs.shiftNo, cs.openedAt, cs.closedAt, cs.cashStart, cs.cashEndExpected, cs.cashEndActual, cs.difference, cs.totalSalesCash, cs.totalSalesCard, cs.status) FROM CashSession cs WHERE cs.businessDate = :businessDate")
    Optional<CashSessionResponse> findCashSessionDTOByBusinessDate(LocalDate businessDate);

    @Query("select coalesce(max(s.shiftNo), 0) from CashSession s where s.businessDate = :bd")
    int maxShiftNoByBusinessDate(@Param("bd") LocalDate businessDate);

    boolean existsByBusinessDateAndStatus(LocalDate businessDate, CashSessionStatus status);

    Optional<CashSession> findFirstByStatusOrderByOpenedAtDesc(CashSessionStatus status);
}
