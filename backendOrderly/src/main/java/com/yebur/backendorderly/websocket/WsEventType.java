package com.yebur.backendorderly.websocket;

public enum WsEventType {
    ORDER_CREATED,
    ORDER_DELETED,
    ORDER_TOTAL_CHANGED,

    ORDER_DETAIL_CREATED,
    ORDER_DETAIL_UPDATED,
    ORDER_DETAIL_DELETED,
    ORDER_DETAIL_STATUS_CHANGED
}
