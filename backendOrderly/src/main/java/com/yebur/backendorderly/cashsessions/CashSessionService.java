package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCount;
import com.yebur.backendorderly.cashcount.CashCountService;
import com.yebur.backendorderly.cashoperations.CashOperation;
import com.yebur.backendorderly.cashoperations.CashOperationRepository;
import com.yebur.backendorderly.orderdetail.OrderDetailRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("cashSessionService")
public class CashSessionService implements CashSessionServiceInterface {
    private final CashSessionRepository cashSessionRepository;
    private final CashOperationRepository cashOperationRepository;
    private final CashCountService cashCountService;
    private final OrderDetailRepository orderDetailRepository;

    public CashSessionService(CashSessionRepository cashSessionRepository, CashCountService cashCountService, OrderDetailRepository orderDetailRepository, CashOperationRepository cashOperationRepository) {
        this.cashSessionRepository = cashSessionRepository;
        this.cashCountService = cashCountService;
        this.orderDetailRepository = orderDetailRepository;
        this.cashOperationRepository = cashOperationRepository;
    }

    @Override
    public Optional<CashSession> findCashSessionById(Long id){
        return cashSessionRepository.findById(id);
    }

    @Override
    public Optional<CashSessionResponse> findCashSessionDTOById(Long id){
        return cashSessionRepository.findCashSessionDTOById(id);
    }

    @Override
    public List<CashSessionResponse> findAllCashSessionDTO(){
        return cashSessionRepository.findAllCashSessionDTO();
    }

    @Override
    public Optional<CashSessionResponse> findCashSessionDTOByBusinessDate(LocalDate businessDate){
        return cashSessionRepository.findCashSessionDTOByBusinessDate(businessDate);
    }

    @Override
    public boolean existsCashSessionByStatus(CashSessionStatus status){
        return cashSessionRepository.existsCashSessionByStatus(status);
    }

    @Override
    public Optional<CashSessionResponse> findCashSessionDTOByStatus(CashSessionStatus status){
        return cashSessionRepository.findCashSessionDTOByStatus(status);
    }

    @Override
    public CashSessionResponse create(CashSessionRequest request){
        return new CashSessionResponse();
    }

    @Override
    public CashSessionResponse open() {
        BigDecimal cashStart = BigDecimal.ZERO;
        try {
            cashStart = cashCountService.getLastCashCountTotal();
        }catch (Exception e){
            System.out.println(e.getMessage());
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

    @Override
    public CashSessionResponse close(Long id, CashCount cashCount){

        CashSession cs = findCashSessionById(id)
                .orElseThrow(() -> new RuntimeException("CashSession not found by this id: " + id));

        cs.setClosedAt(LocalDateTime.now());

        BigDecimal cashSales = orderDetailRepository.getPaidSalesByCashSessionAndPaymentMethod(id, "CASH");
        BigDecimal cardSales = orderDetailRepository.getPaidSalesByCashSessionAndPaymentMethod(id, "CARD");

        BigDecimal opsNet = cashOperationRepository.getNetAmountByCashSession(id);

        BigDecimal cashExpected = cs.getCashStart()
                .add(cashSales)
                .add(opsNet);

        BigDecimal cashActual = cashCountService.getTotal(cashCount);

        BigDecimal totalExpected = cashExpected.add(cardSales);
        BigDecimal totalActual   = cashActual.add(cardSales);

        BigDecimal difference = totalActual.subtract(totalExpected);

        cs.setCashEndExpected(cashExpected);
        cs.setCashEndActual(cashActual);
        cs.setTotalSalesCash(cashSales);
        cs.setTotalSalesCard(cardSales);
        cs.setStatus(CashSessionStatus.CLOSED);
        cs.setDifference(difference);

        cashSessionRepository.save(cs);

        return CashSessionResponse.fromEntity(cs);
    }


    @Override
    public CashSessionResponse update(Long id, CashSessionRequest request){
        CashSession toUpdate = findCashSessionById(id).orElseThrow(() -> new RuntimeException("CashSession not found with this id: " + id));
        toUpdate.setClosedAt(request.getClosedAt());
        toUpdate.setCashStart(request.getCashStart());
        toUpdate.setCashEndExpected(request.getCashEndExpected());
        toUpdate.setCashEndActual(request.getCashEndActual());
        toUpdate.setTotalSalesCash(request.getTotalSalesCash());
        toUpdate.setTotalSalesCard(request.getTotalSalesCard());
        toUpdate.setStatus(CashSessionStatus.valueOf(request.getStatus()));

        return CashSessionResponse.fromEntity(cashSessionRepository.save(toUpdate));
    }

    @Override
    public void delete(Long id){
        CashSession toDelete = findCashSessionById(id).orElseThrow(() -> new RuntimeException("CashSession not found with this id: " + id));

        cashSessionRepository.delete(toDelete);
    }

    public Long findLastOpenCashSessionId(){
        return findCashSessionDTOByStatus(CashSessionStatus.OPEN).map(CashSessionResponse::getId).orElse(null);
    }
}
