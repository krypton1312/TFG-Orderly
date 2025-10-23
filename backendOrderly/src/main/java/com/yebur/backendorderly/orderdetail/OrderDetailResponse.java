package com.yebur.backendorderly.orderdetail;

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

    private double unitPrice;

    private String status;

    public OrderDetailResponse(Long id, Long productId, String productName, Long orderId, String comment, int amount,
            double unitPrice, OrderDetailStatus status) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.orderId = orderId;
        this.comment = comment;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.status = (status != null) ? status.toString() : null;
    }

    
}
