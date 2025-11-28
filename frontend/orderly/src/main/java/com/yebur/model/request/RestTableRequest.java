package com.yebur.model.request;

public class RestTableRequest {
    private int number;

    private String status;

    private String position;

    public RestTableRequest() {
    }

    public RestTableRequest(int number, String status, String position) {
        this.number = number;
        this.status = status;
        this.position = position;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "RestTableRequest{" +
                "number=" + number +
                ", status='" + status + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}
