package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yebur.model.request.OrderRequest;
import com.yebur.model.response.OrderResponse;

public class OrderService {
    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static List<OrderResponse> getAllOrders() throws Exception {
        String json = ApiClient.get("/orders");
        return mapper.readValue(json, new TypeReference<List<OrderResponse>>() {}); 
    }

    public static OrderResponse getOrderById(Long id) throws Exception{
        String json = ApiClient.get("/orders/id/" + id);
        return mapper.readValue(json, OrderResponse.class);
    }

    public static OrderResponse createOrder(OrderRequest order) throws Exception{
        String jsonInput = mapper.writeValueAsString(order);
        String jsonResponse = ApiClient.post("/orders", jsonInput);
        System.out.println(jsonResponse);
        return mapper.readValue(jsonResponse, OrderResponse.class);
    }

    public static OrderResponse updateOrder(Long id, OrderResponse order) throws Exception{
        String jsonInput = mapper.writeValueAsString(order);
        String jsonResponse = ApiClient.put("/orders/" + id, jsonInput);
        return mapper.readValue(jsonResponse, OrderResponse.class);
    }
    
}