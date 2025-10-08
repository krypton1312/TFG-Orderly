package com.yebur.model.response;

public class OrderDetail {
    private Long id;
    private Long productId;
    private Long orderId;
    private String comment;
    private int amount;
    private double unitPrice;
    
    public OrderDetail() {
    }


    public OrderDetail(Long id, Long productId, Long orderId, String comment, int amount, double unitPrice) {
        this.id = id;
        this.productId = productId;
        this.orderId = orderId;
        this.comment = comment;
        this.amount = amount;
        this.unitPrice = unitPrice;
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

    
}
