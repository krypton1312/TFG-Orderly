package com.yebur.backendorderly.dto.input;

import java.time.LocalDateTime;
import java.util.List;

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
    private double total;

    @NotBlank
    private Long idEmployee;

    private Long idClient;

    private Long idTable;
}
