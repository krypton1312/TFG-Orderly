package com.yebur.model.response;

public class TableWithOrderResponse {
    private Long tableId;
    private String tableName;
    private OrderSummary order;

    public TableWithOrderResponse() {
    }
    
    public TableWithOrderResponse(Long tableId, String tableName, OrderSummary order) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.order = order;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public OrderSummary getOrder() {
        return order;
    }

    public void setOrder(OrderSummary order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "TableWithOrderResponse [tableId=" + tableId + ", tableName=" + tableName + ", order=" + order + "]";
    }
    
}
