package com.yebur.model.request;

public class OrderDetailRequest {
    private Long productId;
    private Long orderId;
    private String comment;
    private int amount;
    private double unitPrice;

    
    public OrderDetailRequest() {
    }


    public OrderDetailRequest(Long productId, Long orderId, String comment, int amount, double unitPrice) {
        this.productId = productId;
        this.orderId = orderId;
        this.comment = comment;
        this.amount = amount;
        this.unitPrice = unitPrice;
    }


    public Long getProductId() {
        return productId;
    }


    public void setProductId(Long productId) {
        this.productId = productId;
    }


    public Long getOrderId() {
        return orderId;
    }


    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }


    public String getComment() {
        return comment;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }


    public int getAmount() {
        return amount;
    }


    public void setAmount(int amount) {
        this.amount = amount;
    }


    public double getUnitPrice() {
        return unitPrice;
    }


    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    
}
