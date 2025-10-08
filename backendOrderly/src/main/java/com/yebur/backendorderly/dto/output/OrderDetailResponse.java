package com.yebur.backendorderly.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    private Long id;
    
    private Long productId;

    private Long orderId;

    private String comment;
    
    private int amount;

    private double unitPrice;
}
