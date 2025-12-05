package com.yebur.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDetailResponse {
    private Long id;
    private Long productId;
    private String name;
    private Long orderId;
    private String comment;
    private int amount;
    private BigDecimal unitPrice;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String destination;
    private String batchId;

    public OrderDetailResponse(Long id, Long productId, String name, Long orderId, String comment, int amount,
            BigDecimal unitPrice, String status, String paymentMethod, LocalDateTime createdAt, String destination, String batchId) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.orderId = orderId;
        this.comment = comment;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.destination = destination;
        this.batchId = batchId;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "OrderDetailResponse [id=" + id + ", productId=" + productId + ", orderId=" + orderId + ", comment="
                + comment + ", amount=" + amount + ", unitPrice=" + unitPrice + "]";
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public String getDestination() {
        return destination;
    }


    public void setDestination(String destination) {
        this.destination = destination;
    }


    public String getBatchId() {
        return batchId;
    }


    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
