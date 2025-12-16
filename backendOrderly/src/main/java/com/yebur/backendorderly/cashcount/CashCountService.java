package com.yebur.backendorderly.cashcount;

import com.yebur.backendorderly.cashsessions.CashSession;
import com.yebur.backendorderly.cashsessions.CashSessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CashCountService extends CashCountServiceInterface{
    private CashCountRepository cashCountRepository;
    private CashSessionRepository cashSessionRepository;

    public CashCountService(CashCountRepository cashCountRepository, CashSessionRepository cashSessionRepository) {
        this.cashSessionRepository = cashSessionRepository;
        this.cashCountRepository = cashCountRepository;
    }

    public Optional<CashCount> findById(Long id){
        return cashCountRepository.findById(id);
    }

    public Optional<CashCountResponse> findDTOById(Long id){
        return cashCountRepository.findDTOById(id);
    }

    public Optional<CashCountResponse> findDTOBySessionId(Long sessionId){
        return cashCountRepository.findDTOBySessionId(sessionId);
    }

    public List<CashCountResponse> findAllDTO(){
        return cashCountRepository.findAllDTO();
    }

    public CashCountResponse create(CashCountRequest request){
        CashCount cashCount = cashCountRepository.save(mapToEntity(request));
        return CashCountResponse.mapToResponse(cashCount);
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


}
