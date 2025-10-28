package com.yebur.backendorderly.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @NotBlank
    private LocalDateTime datetime;
    
    @NotBlank
    private String state;

    private String paymentMethod;

    @Positive
    private BigDecimal total;

    private Long idEmployee;

    private Long idClient;

    private Long idTable;
}
