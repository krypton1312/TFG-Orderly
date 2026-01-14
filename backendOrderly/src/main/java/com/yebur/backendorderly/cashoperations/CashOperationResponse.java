package com.yebur.backendorderly.cashoperations;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CashOperationResponse {
    private Long id;

    private Long sessionId;

    private String type;

    private String paymentMethod;

    private String description;

    private BigDecimal amount;

    private LocalDateTime createdAt;

    public CashOperationResponse(Long id, Long sessionId, CashOperationType type, String paymentMethod, String description, BigDecimal amount, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.type = getStatusString(type);
        this.paymentMethod = getPaymentMethodEsp(paymentMethod);
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    private String getStatusString(CashOperationType type){
        return switch (type) {
            case WITHDRAW -> "SALIDA";
            case DEPOSIT -> "ENTRADA";
        };
    }

    private String getPaymentMethodEsp(String paymentMethod){
        return switch (paymentMethod){
            case "CARD" -> "TARJETA";
            case "CASH" -> "EFECTIVO";
            default -> "N/A";
        };
    }

    public static CashOperationResponse mapToResponse(CashOperation entity){
        return new CashOperationResponse(
                entity.getId(),
                entity.getSession().getId(),
                entity.getType(),
                entity.getPaymentMethod(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getCreatedAt()
        );
    }
}
