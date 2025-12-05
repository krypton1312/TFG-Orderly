package com.yebur.backendorderly.orderdetail;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderDetailRequest {
    
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    private Long orderId;

    private String name;

    private String comment;

    @Positive
    private int amount;
    
    @Positive
    private BigDecimal unitPrice;

    private String status;

    private String paymentMethod;

    private String batchId;

    private LocalDateTime createdAt;

    public OrderDetailRequest(Long productId, Long orderId, String name, String comment, int amount, BigDecimal unitPrice, String status, String paymentMethod, String batchId) {
        this.productId = productId;
        this.orderId = orderId;
        this.name = name;
        this.comment = comment;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.batchId = batchId;
    }
}
