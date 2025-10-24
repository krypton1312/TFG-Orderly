package com.yebur.service;

import java.util.List;
import java.util.stream.Collectors;

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

    public static List<OrderDetailResponse> getAllOrderDetailsByOrderId(Long orderId) throws Exception {
        String json = ApiClient.get("/orderDetails/order/" + orderId);
        return mapper.readValue(json, new TypeReference<List<OrderDetailResponse>>() {
        });
    }

    public static List<OrderDetailResponse> getUnpaidOrderDetailsByOrderId(Long orderId) throws Exception {
        String json = ApiClient.get("/orderDetails/order/" + orderId + "/unpaid");
        return mapper.readValue(json, new TypeReference<List<OrderDetailResponse>>() {
        });
    }

    public static OrderDetailResponse createOrderDetail(OrderDetailRequest orderDetail) throws Exception {
        String jsonInput = mapper.writeValueAsString(orderDetail);
        String jsonResponse = ApiClient.post("/orderDetails", jsonInput);
        System.out.println(jsonResponse);
        return mapper.readValue(jsonResponse, OrderDetailResponse.class);
    }

    public static OrderDetailResponse updateOrderDetail(Long id, OrderDetailRequest orderDetail) throws Exception {
        String jsonInput = mapper.writeValueAsString(orderDetail);
        String jsonResponse = ApiClient.put("/orderDetails/" + id, jsonInput);
        return mapper.readValue(jsonResponse, OrderDetailResponse.class);
    }

    public static void changeOrderDetailStatus(List<Long> ids, String status) throws Exception {
        String jsonInput = mapper.writeValueAsString(ids);

        ApiClient.put("/orderDetails/change-status/" + status, jsonInput);
    }

    public static void deleteOrderDetail(Long id) throws Exception {
        ApiClient.delete("/orderDetails/" + id);
    }
}
