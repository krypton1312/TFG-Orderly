package com.yebur.model.response;

public class CategoryResponse {
    private Long id;
    private String name;
    private String color;
    private Integer index;
    
    public CategoryResponse() {
    }

    public CategoryResponse(Long id,String name, String color, Integer index) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.index = index;
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
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public Integer getIndex() {
        return index;
    }
    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Category [id=" + id + ", name=" + name + ", color=" + color + ", index=" + index + "]";
    }
}
