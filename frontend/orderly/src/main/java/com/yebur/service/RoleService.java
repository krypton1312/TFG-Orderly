package com.yebur.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.response.RoleResponse;

import java.util.List;

public class RoleService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<RoleResponse> getAllRoles() throws Exception {
        String json = ApiClient.get("/roles");
        return mapper.readValue(json, new TypeReference<List<RoleResponse>>() {});
    }
}
