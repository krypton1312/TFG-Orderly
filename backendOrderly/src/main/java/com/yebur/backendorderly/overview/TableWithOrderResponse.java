package com.yebur.backendorderly.overview;

import java.math.BigDecimal;

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
    private BigDecimal total;
}
