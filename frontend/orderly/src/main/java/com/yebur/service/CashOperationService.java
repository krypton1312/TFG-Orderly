package com.yebur.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.yebur.model.request.CashOperationRequest;
import com.yebur.model.response.CashOperationResponse;

import java.util.List;

public class CashOperationService {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static List<CashOperationResponse> getAllCashOperations() throws Exception {
        String json = ApiClient.get("/cashOperation");
        return mapper.readValue(json, new TypeReference<List<CashOperationResponse>>() {});
    }

    public static CashOperationResponse getCashOperationById(Long id) throws Exception {
        String json = ApiClient.get("/cashOperation/id/" + id);
        return mapper.readValue(json, CashOperationResponse.class);
    }

    public static List<CashOperationResponse> getCashOperationsBySessionId(Long id) throws Exception {
        String json = ApiClient.get("/cashOperation/cashSession/id/" + id);
        return mapper.readValue(json, new TypeReference<List<CashOperationResponse>>() {});
    }


    public static CashOperationResponse createCashOperation(CashOperationRequest request) throws Exception {
        String jsonInput = mapper.writeValueAsString(request);
        String jsonResponse = ApiClient.post("/cashOperation", jsonInput);
        return mapper.readValue(jsonResponse, CashOperationResponse.class);
    }

    public static CashOperationResponse updateCashOperation(Long id, CashOperationRequest request) throws Exception {
        String jsonInput = mapper.writeValueAsString(request);
        String jsonResponse = ApiClient.put("/cashOperation/id/" + id, jsonInput);
        return mapper.readValue(jsonResponse, CashOperationResponse.class);
    }

    public static void deleteCashOperation(Long id) throws Exception {
        ApiClient.delete("/cashOperation/id/" + id);
    }
}
