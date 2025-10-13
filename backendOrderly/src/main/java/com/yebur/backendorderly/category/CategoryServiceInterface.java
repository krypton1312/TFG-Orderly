package com.yebur.backendorderly.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryServiceInterface {
    
    List<Category> findAll();

    Page<CategoryResponse> findAllCategoryDTOPage(Pageable pageable);

    List<CategoryResponse> findAllCategoryDTO();

    Optional<Category> findById(Long id);

    Optional<CategoryResponse> findCategoryDTOById(Long id);
    
    Category createCategory(Category category);
    
    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);
}
