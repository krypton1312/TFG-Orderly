package com.yebur.model.response;

public class ProductResponse {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    private Long categoryId;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, Double price, Integer stock, Long categoryId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
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
    
    @Override
    public String toString() {
        return "Product [id=" + id + ", name=" + name + ", price=" + price + ", categoryId=" + categoryId + "]";
    }
}
