package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.request.RestTableRequest;
import com.yebur.model.response.ApiException;
import com.yebur.model.response.ErrorResponse;
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
        try {
            String jsonResponse = ApiClient.post("/tables", jsonInput);
            return mapper.readValue(jsonResponse, RestTableResponse.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 400) {
                ErrorResponse error = null;
                try {
                    error = mapper.readValue(e.getResponseBody(), ErrorResponse.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Ошибка 400: " + e.getResponseBody());
                }

                throw new RuntimeException(error.getError());
            } else {
                throw new RuntimeException("Ошибка сервера: " + e.getMessage());
            }
        }
    }

    public static RestTableResponse updateTable(Long id, RestTableRequest table) throws Exception {
        String jsonInput = mapper.writeValueAsString(table);
        try {
            String jsonResponse = ApiClient.put("/tables/id/" + id, jsonInput);
            return mapper.readValue(jsonResponse, RestTableResponse.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 400) {
                ErrorResponse error = mapper.readValue(e.getResponseBody(), ErrorResponse.class);
                throw new RuntimeException(error.getError());
            } else {
                throw new RuntimeException("Ошибка сервера: " + e.getMessage());
            }
        }
    }

    public static void deleteTable(Long id) throws Exception {
        try {
            ApiClient.delete("/tables/id/" + id);
        } catch (ApiException e) {
            throw new RuntimeException("Не удалось удалить стол: " + e.getMessage());
        }
    }
}
