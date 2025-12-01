package com.yebur.model.response;

import java.math.BigDecimal;
import java.util.List;

public class SupplementResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private List<Long> categoriesId;
    private List<Long> productId;

    public SupplementResponse(Long id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public SupplementResponse() {
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<Long> getCategoriesId() {
        return categoriesId;
    }

    public void setCategoriesId(List<Long> categoriesId) {
        this.categoriesId = categoriesId;
    }

    public List<Long> getProductId() {
        return productId;
    }

    public void setProductId(List<Long> productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "SupplementResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", categoriesId=" + categoriesId +
                ", productId=" + productId +
                '}';
    }
}
