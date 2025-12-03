package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.response.ProductsWithSupplements;
import com.yebur.model.response.TableWithOrderResponse;

public class OverviewService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<TableWithOrderResponse> getOverview() throws Exception {
        String json = ApiClient.get("/overview");
        return mapper.readValue(json, new TypeReference<List<TableWithOrderResponse>>() {}); 
    }

    public static ProductsWithSupplements getProductsWithSupplements(Long id) throws Exception{
        String json = ApiClient.get("/overview/products-with-supplements-by-category/id/" + id);
        return mapper.readValue(json, ProductsWithSupplements.class);
    }
}
