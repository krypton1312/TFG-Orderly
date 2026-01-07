package com.yebur.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashSessionResponse {
    private Long id;

    private LocalDate businessDate;

    private Integer shiftNo;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    private BigDecimal cashStart;

    private BigDecimal cashEndExpected;

    private BigDecimal cashEndActual;

    private BigDecimal difference;

    private BigDecimal totalSalesCash;

    private BigDecimal totalSalesCard;

    private String status;
}
