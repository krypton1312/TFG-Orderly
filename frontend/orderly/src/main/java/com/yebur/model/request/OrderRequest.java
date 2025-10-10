package com.yebur.model.request;

public class OrderRequest {
    private String state;
    private String paymentMethod;
    private double total;
    private Long idEmployee;
    private Long idClient;
    private Long idTable;
    
    public OrderRequest() {
    }

    

    public OrderRequest(String state) {
        this.state = state;
        this.total = 0.0;
        this.paymentMethod = null;
        this.idClient = null;
        this.idTable = null;
        this.idEmployee = null;
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
        return idClient;
    }
    public void setIdClient(Long idClient) {
        this.idClient = idClient;
    }
    public Long getIdTable() {
        return idTable;
    }
    public void setIdTable(Long idTable) {
        this.idTable = idTable;
    }
    @Override
    public String toString() {
        return "OrderRequest [state=" + state + ", paymentMethod=" + paymentMethod + ", total=" + total
                + ", idEmployee=" + idEmployee + ", idClient=" + idClient + ", idTable=" + idTable + "]";
    }

    
}
