package com.yebur.model.response;

import java.math.BigDecimal;

public class OrderSummary {
    private Long orderId;
    private BigDecimal total;
    
    public OrderSummary() {
    }

    public OrderSummary(Long orderId, BigDecimal total) {
        this.orderId = orderId;
        this.total = total;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    @Override
    public String toString() {
        return "OrderSummary [orderId=" + orderId + ", total=" + total + "]";
    }
}
