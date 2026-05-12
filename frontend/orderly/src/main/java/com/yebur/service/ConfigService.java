package com.yebur.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.request.ConfigRequest;
import com.yebur.model.response.ConfigResponse;
import com.yebur.model.response.SmtpTestResult;

public class ConfigService {

    private static final ObjectMapper mapper = new ObjectMapper();

    private ConfigService() {}

    public static ConfigResponse getConfig() throws Exception {
        String json = ApiClient.get("/config");
        return mapper.readValue(json, ConfigResponse.class);
    }

    public static ConfigResponse saveConfig(ConfigRequest request) throws Exception {
        String body = mapper.writeValueAsString(request);
        String json = ApiClient.put("/config", body);
        return mapper.readValue(json, ConfigResponse.class);
    }

    public static SmtpTestResult testSmtp() throws Exception {
        String json = ApiClient.post("/config/test-smtp");
        return mapper.readValue(json, SmtpTestResult.class);
    }
}
