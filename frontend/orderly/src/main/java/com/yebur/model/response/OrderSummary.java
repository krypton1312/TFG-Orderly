package com.yebur.model.response;

public class OrderSummary {
    private Long orderId;
    private double total;
    
    public OrderSummary() {
    }

    public OrderSummary(Long orderId, double total) {
        this.orderId = orderId;
        this.total = total;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
    
    @Override
    public String toString() {
        return "OrderSummary [orderId=" + orderId + ", total=" + total + "]";
    }
}
