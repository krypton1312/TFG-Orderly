package com.yebur.backendorderly.order;

import java.time.LocalDateTime;

import com.yebur.backendorderly.resttable.RestTableResponse;

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

    private Double total;

    private Long idEmployee;

    private Long idClient;

    private RestTableResponse restTable;

    public OrderResponse(Long id, LocalDateTime datetime, String state, String paymentMethod, double total,
            Long idEmployee, Long idClient, Long idRestTable) {
        this.id = id;
        this.datetime = datetime;
        this.state = state;
        this.paymentMethod = paymentMethod;
        this.total = total;
        this.idEmployee = idEmployee;
        this.idClient = idClient;
        this.restTable = new RestTableResponse(idRestTable);
    }

    
}
