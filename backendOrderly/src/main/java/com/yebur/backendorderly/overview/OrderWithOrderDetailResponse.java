package com.yebur.backendorderly.overview;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderWithOrderDetailResponse {
    private String overviewId;
    private Long orderId;
    private String tableName;
    private List<OrderDetailSummary> details;
    private LocalDateTime datetime;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class OrderDetailSummary {
    private Long id;
    private String productName;
    private String comment;
    private int amount;
    private String status;
    private String destination;
}
