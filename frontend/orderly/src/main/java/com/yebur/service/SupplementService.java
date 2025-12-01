package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.request.SupplementRequest;
import com.yebur.model.response.SupplementResponse;

public class SupplementService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<SupplementResponse> getAllSupplements() throws Exception {
        String json = ApiClient.get("/supplements");
        return mapper.readValue(json, new TypeReference<List<SupplementResponse>>() {});
    }

    public static SupplementResponse getSupplementById(Long id) throws Exception {
        String json = ApiClient.get("/supplements/" + id);
        return mapper.readValue(json, SupplementResponse.class);
    }

    public static List<SupplementResponse> getSupplementsByCategory(Long categoryId) throws Exception {
        String json = ApiClient.get("/supplements/category/" + categoryId);
        return mapper.readValue(json, new TypeReference<List<SupplementResponse>>() {});
    }


    public static SupplementResponse createSupplement(SupplementRequest Supplement) throws Exception {
        String jsonInput = mapper.writeValueAsString(Supplement);
        System.out.println(jsonInput);
        String jsonResponse = ApiClient.post("/supplements", jsonInput);
        return mapper.readValue(jsonResponse, SupplementResponse.class);
    }

    public static SupplementResponse updateSupplement(Long id, SupplementRequest Supplement) throws Exception {
        String jsonInput = mapper.writeValueAsString(Supplement);
        String jsonResponse = ApiClient.put("/supplements/id/" + id, jsonInput);
        return mapper.readValue(jsonResponse, SupplementResponse.class);
    }

    public static void deleteSupplement(Long id) throws Exception {
        ApiClient.delete("/supplements/id/" + id);
    }
}
