package com.yebur.backendorderly.orderdetail;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderDetailResponse {

    private Long id;
    
    private Long productId;

    private String productName;

    private Long orderId;

    private String comment;
    
    private int amount;

    private BigDecimal unitPrice;

    private String status;

    private String paymentMethod;

    private LocalDateTime createdAt;

    public OrderDetailResponse(Long id, Long productId, String productName, Long orderId, String comment, int amount,
            BigDecimal unitPrice, OrderDetailStatus status, String paymentMethod, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.orderId = orderId;
        this.comment = comment;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.status = (status != null) ? status.toString() : null;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
    }
    
}
