package com.yebur.backendorderly.cashcount;

import com.yebur.backendorderly.cashsessions.CashSession;
import com.yebur.backendorderly.cashsessions.CashSessionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("cashCountService")
public class CashCountService implements CashCountServiceInterface{
    private final CashCountRepository cashCountRepository;
    private final CashSessionRepository cashSessionRepository;

    public CashCountService(CashCountRepository cashCountRepository, CashSessionRepository cashSessionRepository) {
        this.cashSessionRepository = cashSessionRepository;
        this.cashCountRepository = cashCountRepository;
    }

    @Override
    public Optional<CashCount> findCashCountById(Long id){
        return cashCountRepository.findById(id);
    }

    @Override
    public Optional<CashCountResponse> findCashCountDTOById(Long id){
        return cashCountRepository.findDTOById(id);
    }

    @Override
    public Optional<CashCountResponse> findCashCountDTOBySessionId(Long sessionId){
        return cashCountRepository.findDTOBySessionId(sessionId);
    }

    @Override
    public List<CashCountResponse> findAllCashCountDTO(){
        return cashCountRepository.findAllDTO();
    }

    @Override
    public CashCountResponse create(CashCountRequest request){
        CashCount cashCount = cashCountRepository.save(mapToEntity(request));
        return CashCountResponse.mapToResponse(cashCount);
    }

    @Override
    public CashCountResponse update(Long id, CashCountRequest request) {

        CashCount cc = cashCountRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("CashCount not found with id: " + id)
                );

        cc.setCreatedAt(LocalDateTime.now());

        cc.setC001(request.getC001());
        cc.setC002(request.getC002());
        cc.setC005(request.getC005());
        cc.setC010(request.getC010());
        cc.setC020(request.getC020());
        cc.setC050(request.getC050());
        cc.setC100(request.getC100());
        cc.setC200(request.getC200());

        cc.setB005(request.getB005());
        cc.setB010(request.getB010());
        cc.setB020(request.getB020());
        cc.setB050(request.getB050());
        cc.setB100(request.getB100());
        cc.setB200(request.getB200());
        cc.setB500(request.getB500());

        CashCount saved = cashCountRepository.save(cc);

        return CashCountResponse.mapToResponse(saved);
    }

    public void delete(Long id){
        CashCount toDelete = findCashCountById(id).orElseThrow(() -> new IllegalArgumentException("CashCount not found with id: " + id));
        cashCountRepository.delete(toDelete);
    }


    @Override
    public BigDecimal getLastCashCountTotal() {

        CashCount cc = cashCountRepository
                .findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("No CashCount found"));

        BigDecimal total = BigDecimal.ZERO;

        total = total.add(bd("0.01").multiply(qty(cc.getC001())));
        total = total.add(bd("0.02").multiply(qty(cc.getC002())));
        total = total.add(bd("0.05").multiply(qty(cc.getC005())));
        total = total.add(bd("0.10").multiply(qty(cc.getC010())));
        total = total.add(bd("0.20").multiply(qty(cc.getC020())));
        total = total.add(bd("0.50").multiply(qty(cc.getC050())));
        total = total.add(bd("1.00").multiply(qty(cc.getC100())));
        total = total.add(bd("2.00").multiply(qty(cc.getC200())));

        total = total.add(bd("5.00").multiply(qty(cc.getB005())));
        total = total.add(bd("10.00").multiply(qty(cc.getB010())));
        total = total.add(bd("20.00").multiply(qty(cc.getB020())));
        total = total.add(bd("50.00").multiply(qty(cc.getB050())));
        total = total.add(bd("100.00").multiply(qty(cc.getB100())));
        total = total.add(bd("200.00").multiply(qty(cc.getB200())));
        total = total.add(bd("500.00").multiply(qty(cc.getB500())));

        return total;
    }


    public CashCount mapToEntity(CashCountRequest request){
        CashSession session = cashSessionRepository.findById(request.getSessionId()).orElseThrow();
        return new CashCount(
                null,
                session,
                LocalDateTime.now(),
                request.getC001(),
                request.getC002(),
                request.getC005(),
                request.getC010(),
                request.getC020(),
                request.getC050(),
                request.getC100(),
                request.getC200(),
                request.getB005(),
                request.getB010(),
                request.getB020(),
                request.getB050(),
                request.getB100(),
                request.getB200(),
                request.getB500()
        );
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal qty(Integer value) {
        return BigDecimal.valueOf(value == null ? 0 : value);
    }

}
