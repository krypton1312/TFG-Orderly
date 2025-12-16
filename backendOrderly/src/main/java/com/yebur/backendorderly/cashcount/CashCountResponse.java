package com.yebur.backendorderly.cashcount;

import com.yebur.backendorderly.cashsessions.CashSession;
import com.yebur.backendorderly.cashsessions.CashSessionResponse;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashCountResponse {
    private Long id;
    private Long session_id;

    private LocalDateTime createdAt;

    private Integer c001;
    private Integer c002;
    private Integer c005;
    private Integer c010;
    private Integer c020;
    private Integer c050;
    private Integer c100;
    private Integer c200;

    private Integer b005;
    private Integer b010;
    private Integer b020;
    private Integer b050;
    private Integer b100;
    private Integer b200;
    private Integer b500;

    public static CashCountResponse mapToResponse(CashCount cc){
        return new CashCountResponse(
                cc.getId(),
                cc.getSession().getId(),
                cc.getCreatedAt(),
                cc.getC001(),
                cc.getC002(),
                cc.getC005(),
                cc.getC010(),
                cc.getC020(),
                cc.getC050(),
                cc.getC100(),
                cc.getC200(),
                cc.getB005(),
                cc.getB010(),
                cc.getB020(),
                cc.getB050(),
                cc.getB100(),
                cc.getB200(),
                cc.getB500()
        );
    }
}
