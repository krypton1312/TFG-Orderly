package com.yebur.model.response;

public class OrderDetailResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long orderId;
    private String comment;
    private int amount;
    private double unitPrice;
    private String status;
    private String paymentMethod;

    public OrderDetailResponse(Long id, Long productId, String productName, Long orderId, String comment, int amount,
            double unitPrice, String status, String paymentMethod) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.orderId = orderId;
        this.comment = comment;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public OrderDetailResponse() {
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "OrderDetailResponse [id=" + id + ", productId=" + productId + ", orderId=" + orderId + ", comment="
                + comment + ", amount=" + amount + ", unitPrice=" + unitPrice + "]";
    }


    public String getProductName() {
        return productName;
    }


    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
}
