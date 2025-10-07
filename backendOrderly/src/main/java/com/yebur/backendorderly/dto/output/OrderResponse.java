package com.yebur.backendorderly.dto.output;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private Long id;

    private LocalDateTime datetime;
    
    private String state;

    private String paymentMethod;

    private double total;

    private Long idEmployee;

    private Long idClient;

    private Long idTable;
}
