package com.yebur.model.response;

public class RestTableResponse {
    private Long id;
    
    private int number;

    private String name;

    private String status;

    public RestTableResponse() {
    }

    public RestTableResponse(Long id, int number, String name, String status) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RestTableResponse [id=" + id + ", number=" + number + ", name=" + name + ", status=" + status + "]";
    }
    
    
}
