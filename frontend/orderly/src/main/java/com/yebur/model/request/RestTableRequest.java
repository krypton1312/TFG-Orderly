package com.yebur.model.request;

public class RestTableRequest {
    int number;

    String status;

    public RestTableRequest() {
    }

    public RestTableRequest(int number, String status) {
        this.number = number;
        this.status = status;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
