package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.response.CategoryResponse;

public class CategoryService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<CategoryResponse> getAllCategories() throws Exception {
        String json = ApiClient.get("/categories");
        return mapper.readValue(json, new TypeReference<List<CategoryResponse>>() {}); 
    }

    public static List<CategoryResponse> getAllCategoriesPage(Integer page, Integer pageSize) throws Exception{
        String json = ApiClient.get("/categories/page/" + page + "," + pageSize);
        return mapper.readValue(json, new TypeReference<List<CategoryResponse>>() {});
    }

    public static CategoryResponse createCategory(CategoryResponse category) throws Exception {
        String jsonInput = mapper.writeValueAsString(category);
        String jsonResponse = ApiClient.post("/categories", jsonInput);
        return mapper.readValue(jsonResponse, CategoryResponse.class);
    }

    public static CategoryResponse updateCategory(Long id, CategoryResponse category) throws Exception {
        String jsonInput = mapper.writeValueAsString(category);
        String jsonResponse = ApiClient.put("/categories/" + id, jsonInput);
        return mapper.readValue(jsonResponse, CategoryResponse.class);
    }

    public static void deleteCategory(Long id) throws Exception {
        ApiClient.delete("/categories/" + id);
    }
}
