package com.yebur.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CategoryRequest {
    private String name;
    private String color;
    private Integer index;

    public CategoryRequest(String name, String color, Integer index) {
        this.name = name;
        this.color = color;
        this.index = index;
    }

    public CategoryRequest() {
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
        return "CategoryRequest{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", index=" + index +
                '}';
    }
}
