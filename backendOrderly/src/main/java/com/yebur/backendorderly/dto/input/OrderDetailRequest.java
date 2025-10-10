package com.yebur.backendorderly.dto.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailRequest {
    
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    private Long orderId;

    private String comment;

    @Positive
    private int amount;
    
    @Positive
    private double unitPrice;

}
