package com.yebur.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yebur.model.request.CashSessionRequest;
import com.yebur.model.response.CashSessionResponse;

import java.time.LocalDate;
import java.util.List;

public class CashSessionService {

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static List<CashSessionResponse> getAllCashSessions() throws Exception {
        String json = ApiClient.get("/cashSession");
        return mapper.readValue(json, new TypeReference<List<CashSessionResponse>>() {});
    }

    public static CashSessionResponse getCashSessionById(Long id) throws Exception {
        String json = ApiClient.get("/cashSession/id/" + id);
        return mapper.readValue(json, CashSessionResponse.class);
    }

    public static CashSessionResponse getCashSessionByBusinessDate(LocalDate date) throws Exception {
        String json = ApiClient.get("/cashSession/businessDate/" + date);
        return mapper.readValue(json, CashSessionResponse.class);
    }

    public static boolean existsByStatus(String status) throws Exception {
        String json = ApiClient.get("/cashSession/existsByStatus/" + status);
        return mapper.readValue(json, Boolean.class);
    }

    public static CashSessionResponse findCashSessionByStatus(String status) throws Exception{
        String json = ApiClient.get("/cashSession/byStatus/" + status);
        return mapper.readValue(json, CashSessionResponse.class);
    }

    public static CashSessionResponse openCashSession() throws Exception {
        String json = ApiClient.post("/cashSession/open", "");
        return mapper.readValue(json, CashSessionResponse.class);
    }

    public static CashSessionResponse updateCashSession(Long id, CashSessionRequest request) throws Exception {
        String jsonInput = mapper.writeValueAsString(request);
        String jsonResponse = ApiClient.put("/cashSession/id/" + id, jsonInput);
        return mapper.readValue(jsonResponse, CashSessionResponse.class);
    }

    public static void deleteCashSession(Long id) throws Exception {
        ApiClient.delete("/cashSession/id/" + id);
    }
}
