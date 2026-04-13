package com.yebur.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.response.ApiException;

import java.io.IOException;
import java.util.Map;

public class AuthService {

    private AuthService() {}

    public static void login(String email, String password) throws IOException, ApiException {
        String body = new ObjectMapper()
                .writeValueAsString(Map.of("identifier", email, "password", password));
        String response = ApiClient.postNoAuth("/auth/login", body);
        parseAndSave(response);
    }

    public static void refresh(String refreshToken) throws IOException, ApiException {
        String body = new ObjectMapper()
                .writeValueAsString(Map.of("refreshToken", refreshToken));
        String response = ApiClient.postNoAuth("/auth/refresh", body);
        parseAndSave(response);
    }

    private static void parseAndSave(String json) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> map = new ObjectMapper().readValue(json, Map.class);
        SessionStore.save(map.get("token"), map.get("refreshToken"));
    }
}
