package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.request.OrderDetailRequest;
import com.yebur.model.response.OrderDetailResponse;

public class OrderDetailService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<OrderDetailResponse> getAllOrderDetails() throws Exception {
        String json = ApiClient.get("/orderDetails");
        return mapper.readValue(json, new TypeReference<List<OrderDetailResponse>>() {
        });
    }

    public static OrderDetailResponse getOrderDetailsById(Long id) throws Exception {
        String json = ApiClient.get("/orderDetails/" + id);
        return mapper.readValue(json, OrderDetailResponse.class);
    }

    public static List<OrderDetailResponse> getOrderDetailsByOrderId(Long orderId) throws Exception {
        String json = ApiClient.get("/orderDetails/order/" + orderId);
        return mapper.readValue(json, new TypeReference<List<OrderDetailResponse>>(){});
    }

    public static OrderDetailRequest createOrder(OrderDetailRequest orderDetail) throws Exception{
        String jsonInput = mapper.writeValueAsString(orderDetail);
        String jsonResponse = ApiClient.post("/orderDetail", jsonInput);
        return mapper.readValue(jsonResponse, OrderDetailRequest.class);
    }

    public static OrderDetailRequest updateOrder(Long id, OrderDetailRequest orderDetail) throws Exception{
        String jsonInput = mapper.writeValueAsString(orderDetail);
        String jsonResponse = ApiClient.put("/orderDetail/" + id, jsonInput);
        return mapper.readValue(jsonResponse, OrderDetailRequest.class);
    }
}
