package com.yebur.model.response;

public class ClientResponse {
    private Long id;
    private String name;
    private int orderCount;

    public ClientResponse() {
    }

    public ClientResponse(Long id, String name, int orderCount) {
        this.id = id;
        this.name = name;
        this.orderCount = orderCount;
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

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
}
