package com.yebur.model;

import java.time.LocalDateTime;

public class Order {
    private Long id;
    private LocalDateTime datetime;
    private String state;
    private String paymentMethod;
    private double total;
    private Long idEmployee;
    private Long IdClient;
    private Long idTable;

    public Order() {
    }

    public Order(Long id, LocalDateTime datetime, String state, String paymentMethod, double total, Long idEmployee, Long idClient, Long idTable) {
        this.id = id;
        this.datetime = datetime;
        this.state = state;
        this.paymentMethod = paymentMethod;
        this.total = total;
        this.idEmployee = idEmployee;
        IdClient = idClient;
        this.idTable = idTable;
    }

    public Order(LocalDateTime datetime, String state, String paymentMethod, double total, Long idEmployee, Long idClient, Long idTable) {
        this.datetime = datetime;
        this.state = state;
        this.paymentMethod = paymentMethod;
        this.total = total;
        this.idEmployee = idEmployee;
        IdClient = idClient;
        this.idTable = idTable;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getDatetime() {
        return datetime;
    }
    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }
    public Long getIdEmployee() {
        return idEmployee;
    }
    public void setIdEmployee(Long idEmployee) {
        this.idEmployee = idEmployee;
    }
    public Long getIdClient() {
        return IdClient;
    }
    public void setIdClient(Long idClient) {
        IdClient = idClient;
    }
    public Long getIdTable() {
        return idTable;
    }
    public void setIdTable(Long idTable) {
        this.idTable = idTable;
    }
    
    public String toString() {
        return "Order [id=" + id + ", datetime=" + datetime + ", state=" + state + ", paymentMethod=" + paymentMethod
                + ", total=" + total + ", idEmployee=" + idEmployee + ", IdClient=" + IdClient + ", idTable=" + idTable
                + "]";
    }
}
