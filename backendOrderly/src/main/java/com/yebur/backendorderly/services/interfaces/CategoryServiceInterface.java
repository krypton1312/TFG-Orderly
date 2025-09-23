package com.yebur.backendorderly.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.yebur.backendorderly.entities.Category;

public interface CategoryServiceInterface {
    
    List<Category> findAll();

    Optional<Category> findById(Long id);
    
    Category createCategory(Category category);
    
    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);
}
