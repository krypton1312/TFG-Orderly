package com.yebur.backendorderly.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySummaryResponse {
    private int year;
    private int month;
    private BigDecimal totalRevenue;
    private BigDecimal totalSalesCash;
    private BigDecimal totalSalesCard;
    private int orderCount;
    private BigDecimal avgOrderValue;
    private List<TopProductEntry> topProducts;
}
