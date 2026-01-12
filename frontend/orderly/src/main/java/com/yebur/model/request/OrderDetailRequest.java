package com.yebur.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailRequest {
    private Long productId;
    private Long orderId;
    private String name;
    private String comment;
    private int amount;
    private BigDecimal unitPrice;
    private String status;
    private String paymentMethod;
    private String batchId;
    private LocalDateTime createdAt;
    private Long cashSessionId;
}
