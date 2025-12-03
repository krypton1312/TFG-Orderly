package com.yebur.model.response;

import java.util.List;

public class ProductsWithSupplements {
    List<ProductResponse> products;
    List<SupplementResponse> supplements;

    public ProductsWithSupplements(List<ProductResponse> products, List<SupplementResponse> supplements) {
        this.products = products;
        this.supplements = supplements;
    }

    public ProductsWithSupplements() {
    }

    public List<SupplementResponse> getSupplements() {
        return supplements;
    }

    public void setSupplements(List<SupplementResponse> supplements) {
        this.supplements = supplements;
    }

    public List<ProductResponse> getProducts() {
        return products;
    }

    public void setProducts(List<ProductResponse> products) {
        this.products = products;
    }
}
