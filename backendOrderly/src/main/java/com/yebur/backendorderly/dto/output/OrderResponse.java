package com.yebur.backendorderly.dto.output;

import java.time.LocalDateTime;

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
