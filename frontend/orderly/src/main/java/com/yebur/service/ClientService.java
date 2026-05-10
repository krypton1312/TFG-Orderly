package com.yebur.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.response.ClientResponse;

import java.util.List;

public class ClientService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<ClientResponse> getAllClients() throws Exception {
        String json = ApiClient.get("/clients");
        return mapper.readValue(json, new TypeReference<List<ClientResponse>>() {});
    }

    public static ClientResponse getClientById(Long id) throws Exception {
        String json = ApiClient.get("/clients/" + id);
        return mapper.readValue(json, ClientResponse.class);
    }
}
