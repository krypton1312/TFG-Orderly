package com.yebur.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class SupplementResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private List<CategoryResponseSummary> categories;
    private List<ProductResponseSummary> products;

    public SupplementResponse() {
    }

    public SupplementResponse(Long id, String name, BigDecimal price, List<CategoryResponseSummary> categories, List<ProductResponseSummary> products) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categories = categories;
        this.products = products;
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

    public List<CategoryResponseSummary> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponseSummary> categories) {
        this.categories = categories;
    }

    public List<ProductResponseSummary> getProducts() {
        return products;
    }

    public void setProducts(List<ProductResponseSummary> products) {
        this.products = products;
    }
}