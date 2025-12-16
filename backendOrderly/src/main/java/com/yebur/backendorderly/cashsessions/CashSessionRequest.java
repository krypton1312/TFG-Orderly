package com.yebur.backendorderly.cashsessions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashSessionRequest {
    private LocalDateTime closedAt;

    private BigDecimal cashStart;

    private BigDecimal cashEndExpected;

    private BigDecimal cashEndActual;

    private BigDecimal difference;

    private BigDecimal totalSalesCash;

    private BigDecimal totalSalesCard;

    private String status;
}
