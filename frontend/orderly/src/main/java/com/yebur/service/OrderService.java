package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yebur.model.Order;

public class OrderService {
    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static List<Order> getAllOrders() throws Exception {
        String json = ApiClient.get("/orders");
        return mapper.readValue(json, new TypeReference<List<Order>>() {}); 
    }

    public static Order getOrderById(Long id) throws Exception{
        String json = ApiClient.get("/orders/id/" + id);
        return mapper.readValue(json, Order.class);
    }

    public static Order createOrder(Order order) throws Exception{
        String jsonInput = mapper.writeValueAsString(order);
        String jsonResponse = ApiClient.post("/orders", jsonInput);
        return mapper.readValue(jsonResponse, Order.class);
    }

    public static Order updateOrder(Long id, Order order) throws Exception{
        String jsonInput = mapper.writeValueAsString(order);
        String jsonResponse = ApiClient.put("/orders/" + id, jsonInput);
        return mapper.readValue(jsonResponse, Order.class);
    }
    
}