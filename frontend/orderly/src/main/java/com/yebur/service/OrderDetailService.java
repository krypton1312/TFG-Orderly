package com.yebur.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yebur.model.request.OrderDetailRequest;
import com.yebur.model.response.OrderDetailResponse;

public class OrderDetailService {
    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
        return mapper.readValue(jsonResponse, OrderDetailResponse.class);
    }

    public static List<OrderDetailResponse> createOrderDetailList(List<OrderDetailRequest> orderDetails)
            throws Exception {
        String jsonInput = mapper.writeValueAsString(orderDetails);
        String jsonResponse = ApiClient.post("/orderDetails/list", jsonInput);

        return mapper.readValue(jsonResponse,
                mapper.getTypeFactory().constructCollectionType(List.class, OrderDetailResponse.class));
    }

    public static OrderDetailResponse updateOrderDetail(Long id, OrderDetailRequest orderDetail)
            throws Exception {
        String jsonInput = mapper.writeValueAsString(orderDetail);
        String jsonResponse = ApiClient.put("/orderDetails/" + id, jsonInput);
        return mapper.readValue(jsonResponse, OrderDetailResponse.class);
    }

    public static List<OrderDetailResponse> updateOrderDetailList(List<Long> ids, List<OrderDetailRequest> orderDetails) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ids", ids);
        payload.put("details", orderDetails);

        String jsonInput = mapper.writeValueAsString(payload);
        String jsonResponse = ApiClient.post("/orderDetails/update-list", jsonInput);

        return mapper.readValue(
                jsonResponse,
                mapper.getTypeFactory().constructCollectionType(List.class, OrderDetailResponse.class));
    }

    public static void applySupplementLastDetail(Long orderId, Long supplementId) throws Exception {
        ApiClient.post("/orderDetails/apply-supplement-last-detail/"+ orderId + "/supplement/" + supplementId);
    }

    public static void applySupplementToDetail(Long supplementId, List<OrderDetailResponse> orderDetails) throws Exception {
        String  jsonInput = mapper.writeValueAsString(orderDetails);
        ApiClient.post("/orderDetails/apply-supplement-to-details/" + supplementId, jsonInput);
    }

    public static void changeOrderDetailStatus(List<Long> ids, String status) throws Exception {
        String jsonInput = mapper.writeValueAsString(ids);

        ApiClient.put("/orderDetails/change-status/" + status, jsonInput);
    }

    public static void deleteOrderDetail(Long id) throws Exception {
        ApiClient.delete("/orderDetails/" + id);
    }
}
