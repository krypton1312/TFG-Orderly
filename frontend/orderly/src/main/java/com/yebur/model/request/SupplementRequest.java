package com.yebur.model.request;
import java.math.BigDecimal;
import java.util.List;

public class SupplementRequest {
    private String name;
    private BigDecimal price;
    private List<Long> categories_ids;
    private List<Long> products_ids;

    public SupplementRequest(String name, BigDecimal price, List<Long> categories_ids,List<Long> products_ids) {
        this.name = name;
        this.price = price;
        this.categories_ids = categories_ids;
        this.products_ids = products_ids;
    }

    public SupplementRequest() {
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

    public List<Long> getProducts_ids() {
        return products_ids;
    }

    public void setProducts_ids(List<Long> products_ids) {
        this.products_ids = products_ids;
    }

    public List<Long> getCategories_ids() {
        return categories_ids;
    }

    public void setCategories_ids(List<Long> categories_ids) {
        this.categories_ids = categories_ids;
    }

    @Override
    public String toString() {
        return "SupplementRequest{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", categories_ids=" + categories_ids +
                ", products_ids=" + products_ids +
                '}';
    }
}