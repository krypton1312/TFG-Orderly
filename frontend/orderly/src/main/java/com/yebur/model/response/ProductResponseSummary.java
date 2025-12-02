package com.yebur.model.response;

public class ProductResponseSummary {
    private Long id;
    private String name;

    public ProductResponseSummary() {
    }

    public ProductResponseSummary(Long id, String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public String toString() {
        return "ProductResponseSummary{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
