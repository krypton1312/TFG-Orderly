package com.yebur.backendorderly.cashsessions;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
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

    public CashSessionResponse(Long id, LocalDate businessDate, Integer shiftNo, LocalDateTime openedAt, LocalDateTime closedAt, BigDecimal cashStart, BigDecimal cashEndExpected, BigDecimal cashEndActual, BigDecimal difference, BigDecimal totalSalesCash, BigDecimal totalSalesCard, CashSessionStatus status) {
        this.id = id;
        this.businessDate = businessDate;
        this.shiftNo = shiftNo;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.cashStart = cashStart;
        this.cashEndExpected = cashEndExpected;
        this.cashEndActual = cashEndActual;
        this.difference = difference;
        this.totalSalesCash = totalSalesCash;
        this.totalSalesCard = totalSalesCard;
        this.status = getStatusString(status);
    }

    public String getStatusString(CashSessionStatus status){
        switch (status){
            case OPEN -> {
                return "Abierto";
            }
            case CLOSED -> {
                return "Cerrado";
            }
            default -> {
                return "N/A";
            }
        }
    }

    public static CashSessionResponse fromEntity(CashSession s) {
        return new CashSessionResponse(
                s.getId(),
                s.getBusinessDate(),
                s.getShiftNo(),
                s.getOpenedAt(),
                s.getClosedAt(),
                s.getCashStart(),
                s.getCashEndExpected(),
                s.getCashEndActual(),
                s.getDifference(),
                s.getTotalSalesCash(),
                s.getTotalSalesCard(),
                s.getStatus()
        );
    }
}
