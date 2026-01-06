package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCount;
import com.yebur.backendorderly.cashcount.CashCountRequest;
import com.yebur.backendorderly.cashcount.CashCountService;
import com.yebur.backendorderly.cashoperations.CashOperation;
import com.yebur.backendorderly.cashoperations.CashOperationService;
import com.yebur.backendorderly.order.Order;
import com.yebur.backendorderly.orderdetail.OrderDetail;
import com.yebur.backendorderly.orderdetail.OrderDetailStatus;
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
    private final CashCountService cashCountService;
    private final CashOperationService cashOperationService;

    public CashSessionService(CashSessionRepository cashSessionRepository, CashCountService cashCountService, CashOperationService cashOperationService) {
        this.cashSessionRepository = cashSessionRepository;
        this.cashCountService = cashCountService;
        this.cashOperationService = cashOperationService;
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
    public CashSessionResponse create(CashSessionRequest request){
        return new CashSessionResponse();
    }

    @Override
    public CashSessionResponse open() {
        BigDecimal cashStart = cashCountService.getLastCashCountTotal();

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

        CashSession cs = findCashSessionById(id).orElseThrow(() -> new RuntimeException("CashSession not found by this id: " + id));

        cs.setClosedAt(LocalDateTime.now());

        BigDecimal cashEndExpected = BigDecimal.ZERO;

        List<OrderDetail> ods = cs.getOrderDetails();
        int cashSales = 0;
        int cardSales = 0;
        BigDecimal sales = BigDecimal.ZERO;
        for(OrderDetail od: ods){
            if(od.getStatus() == OrderDetailStatus.PAID){
                if(od.getPaymentMethod().equals("CASH")){
                    cardSales++;
                }else{
                    cashSales++;
                }
                sales = sales.add(
                        od.getUnitPrice().multiply(BigDecimal.valueOf(od.getAmount()))
                );
            }
        }

        List<CashOperation> operations = cashOperationService.find

        cashEndExpected.add(cs.getCashStart()).add(sales);


        return new CashSessionResponse();
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
}
