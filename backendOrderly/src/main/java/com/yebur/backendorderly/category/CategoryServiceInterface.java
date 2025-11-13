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
    
    CategoryResponse createCategory(CategoryRequest category);
    
    CategoryResponse updateCategory(Long id, CategoryRequest category);

    void deleteCategory(Long id);
}
