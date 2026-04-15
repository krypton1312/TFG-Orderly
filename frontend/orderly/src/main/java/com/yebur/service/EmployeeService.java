package com.yebur.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yebur.model.request.EmployeeRequest;
import com.yebur.model.response.EmployeeResponse;

import java.util.List;

public class EmployeeService {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static List<EmployeeResponse> getAllEmployees() throws Exception {
        String json = ApiClient.get("/employees");
        return mapper.readValue(json, new TypeReference<List<EmployeeResponse>>() {});
    }

    public static EmployeeResponse createEmployee(EmployeeRequest request) throws Exception {
        String json = ApiClient.post("/employees", mapper.writeValueAsString(request));
        return mapper.readValue(json, EmployeeResponse.class);
    }

    public static EmployeeResponse updateEmployee(Long id, EmployeeRequest request) throws Exception {
        String json = ApiClient.put("/employees/" + id, mapper.writeValueAsString(request));
        return mapper.readValue(json, EmployeeResponse.class);
    }

    public static String resetPassword(Long id) throws Exception {
        String json = ApiClient.post("/employees/" + id + "/reset-password");
        return mapper.readTree(json).get("tempPassword").asText();
    }
}
