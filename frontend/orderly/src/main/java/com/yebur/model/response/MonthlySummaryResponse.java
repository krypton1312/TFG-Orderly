package com.yebur.model.response;

import java.math.BigDecimal;
import java.util.List;

public class MonthlySummaryResponse {
    private int year;
    private int month;
    private BigDecimal totalRevenue;
    private BigDecimal totalSalesCash;
    private BigDecimal totalSalesCard;
    private int orderCount;
    private BigDecimal avgOrderValue;
    private List<TopProductEntry> topProducts;

    public MonthlySummaryResponse() {}

    public MonthlySummaryResponse(int year, int month, BigDecimal totalRevenue,
                                   BigDecimal totalSalesCash, BigDecimal totalSalesCard,
                                   int orderCount, BigDecimal avgOrderValue,
                                   List<TopProductEntry> topProducts) {
        this.year = year;
        this.month = month;
        this.totalRevenue = totalRevenue;
        this.totalSalesCash = totalSalesCash;
        this.totalSalesCard = totalSalesCard;
        this.orderCount = orderCount;
        this.avgOrderValue = avgOrderValue;
        this.topProducts = topProducts;
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalSalesCash() { return totalSalesCash; }
    public void setTotalSalesCash(BigDecimal totalSalesCash) { this.totalSalesCash = totalSalesCash; }

    public BigDecimal getTotalSalesCard() { return totalSalesCard; }
    public void setTotalSalesCard(BigDecimal totalSalesCard) { this.totalSalesCard = totalSalesCard; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }

    public BigDecimal getAvgOrderValue() { return avgOrderValue; }
    public void setAvgOrderValue(BigDecimal avgOrderValue) { this.avgOrderValue = avgOrderValue; }

    public List<TopProductEntry> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProductEntry> topProducts) { this.topProducts = topProducts; }
}
