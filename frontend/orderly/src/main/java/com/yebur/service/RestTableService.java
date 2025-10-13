package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.request.RestTableRequest;
import com.yebur.model.response.RestTableResponse;

public class RestTableService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<RestTableResponse> getAllRestTables() throws Exception {
        String json = ApiClient.get("/tables");
        return mapper.readValue(json, new TypeReference<List<RestTableResponse>>() {}); 
    }

    public static RestTableResponse getRestTableById(Long id) throws Exception {
        String json = ApiClient.get("/tables/id/" + id);
        return mapper.readValue(json, RestTableResponse.class);
    }

    public static RestTableResponse createTable(RestTableRequest table) throws Exception {
        String jsonInput = mapper.writeValueAsString(table);
        String jsonResponse = ApiClient.post("/tables", jsonInput);
        return mapper.readValue(jsonResponse, RestTableResponse.class);
    }

    public static RestTableResponse updateTable(Long id, RestTableRequest table) throws Exception {
        String jsonInput = mapper.writeValueAsString(table);
        String jsonResponse = ApiClient.put("/tables/" + id, jsonInput);
        return mapper.readValue(jsonResponse, RestTableResponse.class);
    }

    public static void deleteTable(Long id) throws Exception {
        ApiClient.delete("/tables/" + id);
    }
}
