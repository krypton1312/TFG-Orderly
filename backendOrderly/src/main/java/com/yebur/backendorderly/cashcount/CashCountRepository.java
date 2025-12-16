package com.yebur.backendorderly.cashcount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashCountRepository extends JpaRepository<CashCount, Long> {
    @Query("SELECT new com.yebur.backendorderly.cashcount.CashCountResponse(cc.id, cc.session.id, cc.createdAt, cc.c001, cc.c002, cc.c005, cc.c010, cc.c020, cc.c050," +
            "cc.c100, cc.c200, cc.b005, cc.b010, cc.b020, cc.b050, cc.b100, cc.b200, cc.b500) FROM CashCount cc")
    List<CashCountResponse> findAllDTO();

    @Query("SELECT new com.yebur.backendorderly.cashcount.CashCountResponse(cc.id, cc.session.id, cc.createdAt, cc.c001, cc.c002, cc.c005, cc.c010, cc.c020, cc.c050," +
            "cc.c100, cc.c200, cc.b005, cc.b010, cc.b020, cc.b050, cc.b100, cc.b200, cc.b500) FROM CashCount cc WHERE cc.session.id = :sessionId")
    Optional<CashCountResponse> findDTOBySessionId(Long sessionId);

    @Query("SELECT new com.yebur.backendorderly.cashcount.CashCountResponse(cc.id, cc.session.id, cc.createdAt, cc.c001, cc.c002, cc.c005, cc.c010, cc.c020, cc.c050," +
            "cc.c100, cc.c200, cc.b005, cc.b010, cc.b020, cc.b050, cc.b100, cc.b200, cc.b500) FROM CashCount cc WHERE cc.id = :id")
    Optional<CashCountResponse> findDTOById(Long id);
}
