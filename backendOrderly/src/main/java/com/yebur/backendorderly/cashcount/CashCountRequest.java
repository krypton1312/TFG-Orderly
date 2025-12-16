package com.yebur.backendorderly.cashcount;

import com.yebur.backendorderly.cashsessions.CashSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashCountRequest {
    private Long sessionId;

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
}
