package com.yebur.model.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDetailRequest {
    private Long productId;
    private Long orderId;
    private String name;
    private String comment;
    private int amount;
    private BigDecimal unitPrice;
    private String status;
    private String paymentMethod;
    private String batchId;
    private LocalDateTime createdAt;

    public OrderDetailRequest() {
    }

    public OrderDetailRequest(Long productId, Long orderId, String name, String comment, int amount, BigDecimal unitPrice, String status, String paymentMethod, String batchId) {
        this.productId = productId;
        this.orderId = orderId;
        this.name = name;
        this.comment = comment;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.batchId = batchId;
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


    public BigDecimal getUnitPrice() {
        return unitPrice;
    }


    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OrderDetailRequest [productId=" + productId + ", orderId=" + orderId + ", comment=" + comment
                + ", amount=" + amount + ", unitPrice=" + unitPrice + ", status=" + status + ", paymentMethod="
                + paymentMethod + ", batchId=" + batchId + "]";
    }
    
    
}
