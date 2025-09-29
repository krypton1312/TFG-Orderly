package com.yebur.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebur.model.Category;

public class CategoryService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Category> getAllCategories() throws Exception {
        String json = ApiClient.get("/categories");
        return mapper.readValue(json, new TypeReference<List<Category>>() {}); 
    }

    public static List<Category> getAllCategoriesPage(Integer page, Integer pageSize) throws Exception{
        String json = ApiClient.get("/categories/page/" + page + "," + pageSize);
        return mapper.readValue(json, new TypeReference<List<Category>>() {});
    }

    public static Category createCategory(Category category) throws Exception {
        String jsonInput = mapper.writeValueAsString(category);
        String jsonResponse = ApiClient.post("/categories", jsonInput);
        return mapper.readValue(jsonResponse, Category.class);
    }

    public static Category updateCategory(Long id, Category category) throws Exception {
        String jsonInput = mapper.writeValueAsString(category);
        String jsonResponse = ApiClient.put("/categories/" + id, jsonInput);
        return mapper.readValue(jsonResponse, Category.class);
    }

    public static void deleteCategory(Long id) throws Exception {
        ApiClient.delete("/categories/" + id);
    }
}
