package com.yebur.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.yebur.model.request.CashCountRequest;
import com.yebur.model.response.CashCountResponse;

import java.util.List;

public class CashCountService {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // GET /cashcounts
    public static List<CashCountResponse> getAllCashCounts() throws Exception {
        String json = ApiClient.get("/cashcounts");
        return mapper.readValue(json, new TypeReference<List<CashCountResponse>>() {});
    }

    // GET /cashcounts/id/{id}
    public static CashCountResponse getCashCountById(Long id) throws Exception {
        String json = ApiClient.get("/cashcounts/id/" + id);
        return mapper.readValue(json, CashCountResponse.class);
    }

    // GET /cashcounts/sessionId/{id}
    public static CashCountResponse getCashCountBySessionId(Long sessionId) throws Exception {
        String json = ApiClient.get("/cashcounts/sessionId/" + sessionId);
        return mapper.readValue(json, CashCountResponse.class);
    }

    // POST /cashcounts
    public static CashCountResponse createCashCount(CashCountRequest request) throws Exception {
        String jsonInput = mapper.writeValueAsString(request);
        String jsonResponse = ApiClient.post("/cashcounts", jsonInput);
        return mapper.readValue(jsonResponse, CashCountResponse.class);
    }

    // PUT /cashcounts/id/{id}
    public static CashCountResponse updateCashCount(Long id, CashCountRequest request) throws Exception {
        String jsonInput = mapper.writeValueAsString(request);
        String jsonResponse = ApiClient.put("/cashcounts/id/" + id, jsonInput);
        return mapper.readValue(jsonResponse, CashCountResponse.class);
    }

    // DELETE /cashcounts/id/{id}
    public static void deleteCashCount(Long id) throws Exception {
        ApiClient.delete("/cashcounts/id/" + id);
    }
}
