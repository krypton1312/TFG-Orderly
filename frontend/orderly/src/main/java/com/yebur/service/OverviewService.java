package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.request.RestTableRequest;
import com.yebur.model.response.RestTableResponse;
import com.yebur.model.response.TableWithOrderResponse;

public class OverviewService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<TableWithOrderResponse> getOverview() throws Exception {
        String json = ApiClient.get("/overview");
        return mapper.readValue(json, new TypeReference<List<TableWithOrderResponse>>() {}); 
    }
}
