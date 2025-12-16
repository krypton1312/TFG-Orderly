package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCountRequest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CashSessionService extends CashSessionServiceInterface {
    private CashSessionRepository cashSessionRepository;

    public CashSessionService(CashSessionRepository cashSessionRepository) {
        this.cashSessionRepository = cashSessionRepository;
    }

    public Optional<CashSession> findCashSessionById(Long id){
        return cashSessionRepository.findById(id);
    }

    public Optional<CashSessionResponse> findCashSessionDTOById(Long id){
        return cashSessionRepository.findCashSessionDTOById(id);
    }

    public List<CashSessionResponse> findAllCashSessionDTO(){
        return cashSessionRepository.findAllCashSessionDTO();
    }

    public Optional<CashSessionResponse> findCashSessionDTOByBusinessDate(LocalDate businessDate){
        return cashSessionRepository.findCashSessionDTOByBusinessDate(businessDate);
    }

    public CashSessionResponse create(CashSessionRequest request){
        return null;
    }

    public CashSessionResponse open(BigDecimal cashStart) {

        if (cashStart == null) {
            throw new IllegalArgumentException("cashStart is required");
        }
        if (cashStart.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("cashStart must be >= 0");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate businessDate = now.toLocalDate();

        boolean openExists = cashSessionRepository.existsByBusinessDateAndStatus(businessDate, CashSessionStatus.OPEN);
        if (openExists) {
            throw new IllegalStateException("There is already an OPEN cash session for " + businessDate);
        }

        int nextShiftNo = cashSessionRepository.maxShiftNoByBusinessDate(businessDate) + 1;

        CashSession s = new CashSession();
        s.setBusinessDate(businessDate);
        s.setShiftNo(nextShiftNo);

        s.setOpenedAt(now);
        s.setClosedAt(null);

        s.setCashStart(cashStart);

        s.setStatus(CashSessionStatus.OPEN);

        try {
            CashSession saved = cashSessionRepository.saveAndFlush(s);
            return CashSessionResponse.fromEntity(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Failed to open session due to concurrent request. Try again.", e);
        }
    }

    /*
    public CashSession mapToEntity(CashSessionRequest request){
        CashSession session = new CashSession();
        session.setClosedAt(request.getClosedAt());
        session.setCashStart(request.getCashStart());
        session.setCashEndExpected(request.getCashEndExpected());
        session.setCashEndActual(request.getCashEndActual());
        session.setDifference(request.getDifference());
        session.setTotalSalesCard(request.getTotalSalesCard());
    }*/
}
