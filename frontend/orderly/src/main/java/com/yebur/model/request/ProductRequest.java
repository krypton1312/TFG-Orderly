package com.yebur.model.request;

import java.math.BigDecimal;

public class ProductRequest {

    private String name;

    private BigDecimal price;

    private Integer stock;

    private Long categoryId;

    private String destination;

    public ProductRequest(String name, BigDecimal price, Integer stock, Long categoryId, String destination) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.destination = destination;
    }

    public ProductRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
    @Override
    public String toString() {
        return "ProductRequest{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", categoryId=" + categoryId +
                '}';
    }
}
