package com.yebur.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashOperationRequest {
    private Long sessionId;

    private String type;

    private String description;

    private BigDecimal amount;
}