package com.yebur.backendorderly.overview;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableWithOrderResponse {
    private Long tableId;
    private String tableName;
    private OrderSummary order;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class OrderSummary {
    private Long orderId;
    private Double total;
}
