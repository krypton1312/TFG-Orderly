package com.yebur.backendorderly.orderdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    private Long id;
    
    private Long productId;

    private String productName;

    private Long orderId;

    private String comment;
    
    private int amount;

    private double unitPrice;
}
