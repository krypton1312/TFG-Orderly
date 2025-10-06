package com.yebur.backendorderly.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailRequest {
    
    @NotBlank
    private Long productId;

    @NotBlank
    private Long orderId;

    private String comment;

    @Positive
    private int amount;
    
    @Positive
    private double unitPrice;

}
