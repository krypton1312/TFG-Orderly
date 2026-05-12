package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCount;
import com.yebur.backendorderly.cashcount.CashCountRepository;
import com.yebur.backendorderly.cashcount.CashCountService;
import com.yebur.backendorderly.cashoperations.CashOperationRepository;
import com.yebur.backendorderly.orderdetail.OrderDetailRepository;
import com.yebur.backendorderly.websocket.WsEvent;
import com.yebur.backendorderly.websocket.WsEventType;
import com.yebur.backendorderly.websocket.WsNotifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final CashCountRepository cashCountRepository;
    private final WsNotifier wsNotifier;

    public CashSessionService(CashSessionRepository cashSessionRepository, CashCountService cashCountService, OrderDetailRepository orderDetailRepository, CashOperationRepository cashOperationRepository, CashCountRepository cashCountRepository, WsNotifier wsNotifier) {
        this.cashSessionRepository = cashSessionRepository;
        this.cashCountService = cashCountService;
        this.orderDetailRepository = orderDetailRepository;
        this.cashOperationRepository = cashOperationRepository;
        this.cashCountRepository = cashCountRepository;
        this.wsNotifier = wsNotifier;
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
        return cashSessionRepository.findFirstByStatusOrderByIdDesc(status)
                .map(CashSessionResponse::fromEntity);
    }

    @Override
    @Transactional
    public CashSessionResponse open() {
        BigDecimal cashStart = BigDecimal.ZERO;
        try {
            cashStart = cashCountService.getLastCashCountTotal();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate businessDate = now.toLocalDate();

        boolean openExists = cashSessionRepository.existsCashSessionByStatus(CashSessionStatus.OPEN);
        if (openExists) {
            throw new IllegalStateException("There is already an OPEN cash session.");
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
            final Long sessionId = saved.getId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    wsNotifier.send(new WsEvent(WsEventType.SESSION_OPENED, null, null, null, sessionId));
                }
            });
            return CashSessionResponse.fromEntity(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Failed to open session due to concurrent request. Try again.", e);
        }
    }

    @Override
    @Transactional
    public CashSessionResponse reopen() {
        if (cashSessionRepository.existsCashSessionByStatus(CashSessionStatus.OPEN)) {
            throw new IllegalStateException("There is already an OPEN cash session.");
        }
        CashSession latest = cashSessionRepository
                .findFirstByStatusOrderByIdDesc(CashSessionStatus.CLOSED)
                .orElseThrow(() -> new RuntimeException("No CLOSED session to reopen."));
        latest.setStatus(CashSessionStatus.OPEN);
        latest.setClosedAt(null);
        CashSession saved = cashSessionRepository.saveAndFlush(latest);
        final Long sessionId = saved.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                wsNotifier.send(new WsEvent(WsEventType.SESSION_OPENED, null, null, null, sessionId));
            }
        });
        return CashSessionResponse.fromEntity(saved);
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

        wsNotifier.send(new WsEvent(WsEventType.SESSION_CLOSED, null, null, null, cs.getId()));

        cashCountRepository.findFirstBySession_Id(cs.getId()).ifPresentOrElse(
            existing -> {
                copyDenominations(existing, cashCount);
                existing.setCreatedAt(LocalDateTime.now());
                cashCountRepository.save(existing);
            },
            () -> {
                cashCount.setSession(cs);
                cashCount.setCreatedAt(LocalDateTime.now());
                cashCountRepository.save(cashCount);
            }
        );

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

    private void copyDenominations(CashCount target, CashCount source) {
        target.setC001(source.getC001());
        target.setC002(source.getC002());
        target.setC005(source.getC005());
        target.setC010(source.getC010());
        target.setC020(source.getC020());
        target.setC050(source.getC050());
        target.setC100(source.getC100());
        target.setC200(source.getC200());
        target.setB005(source.getB005());
        target.setB010(source.getB010());
        target.setB020(source.getB020());
        target.setB050(source.getB050());
        target.setB100(source.getB100());
        target.setB200(source.getB200());
        target.setB500(source.getB500());
    }
}
