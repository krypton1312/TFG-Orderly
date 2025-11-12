package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.request.ProductRequest;
import com.yebur.model.response.ProductResponse;

public class ProductService {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static List<ProductResponse> getAllProducts() throws Exception {
        String json = ApiClient.get("/products");
        return mapper.readValue(json, new TypeReference<List<ProductResponse>>() {}); 
    }

    public static List<ProductResponse> getProductsByCategory(Long id) throws Exception {
        String json = ApiClient.get("/products/byCategory/" + id);
        return mapper.readValue(json, new TypeReference<List<ProductResponse>>() {});
    }

    public static List<ProductResponse> getProductsPageByCategory(Long categoryId, Integer page, Integer pageSize) throws Exception{
        String json = ApiClient.get("/products/categoryId/" + categoryId + "/page/" + page + "," + pageSize);
        return mapper.readValue(json, new TypeReference<List<ProductResponse>>() {});
    }

    public static ProductResponse createProduct(ProductRequest product) throws Exception {
        String jsonInput = mapper.writeValueAsString(product);
        String jsonResponse = ApiClient.post("/products", jsonInput);
        return mapper.readValue(jsonResponse, ProductResponse.class);
    }

    public static ProductResponse updateProduct(Long id, ProductRequest product) throws Exception {
        String jsonInput = mapper.writeValueAsString(product);
        String jsonResponse = ApiClient.put("/products/" + id, jsonInput);
        return mapper.readValue(jsonResponse, ProductResponse.class);
    }
    public static void deleteProduct(Long id) throws Exception {
        ApiClient.delete("/products/" + id);
    }
}
