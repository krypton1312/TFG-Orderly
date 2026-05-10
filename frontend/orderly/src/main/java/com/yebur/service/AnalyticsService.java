package com.yebur.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yebur.model.response.MonthlySummaryResponse;

public class AnalyticsService {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static MonthlySummaryResponse getMonthlySummary(int year, int month) throws Exception {
        String json = ApiClient.get("/analytics/monthly-summary?year=" + year + "&month=" + month);
        return mapper.readValue(json, MonthlySummaryResponse.class);
    }
}
