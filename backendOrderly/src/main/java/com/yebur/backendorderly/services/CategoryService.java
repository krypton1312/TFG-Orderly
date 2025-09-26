package com.yebur.backendorderly.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.dto.output.CategoryResponse;
import com.yebur.backendorderly.entities.Category;
import com.yebur.backendorderly.repositories.CategoryRepository;
import com.yebur.backendorderly.services.interfaces.CategoryServiceInterface;

@Service("categoryService")
public class CategoryService implements CategoryServiceInterface {
    
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAll(){
        return categoryRepository.findAll();
    }

    @Override
    public List<CategoryResponse> findAllCategoryDTO() {
        return categoryRepository.findAllCategoryDTO();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<CategoryResponse> findCategoryDTOById(Long id) {
        return categoryRepository.findCategoryDTOById(id);
    }

    @Override
    public Category createCategory(Category category){
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id " + id));

        existingCategory.setName(category.getName());
        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(Long id){
        categoryRepository.deleteById(id);
    }
}
