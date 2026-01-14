package com.yebur.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashOperationResponse {
    private Long id;

    private Long sessionId;

    private String type;

    private String paymentMethod;

    private String description;

    private BigDecimal amount;

    private LocalDateTime createdAt;

}
