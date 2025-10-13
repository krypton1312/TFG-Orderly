package com.yebur.backendorderly.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service("categoryService")
public class CategoryService implements CategoryServiceInterface {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<CategoryResponse> findAllCategoryDTOPage(Pageable pageable) {
        return categoryRepository.findAllCategoryDTOPage(pageable);
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
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id " + id));

        existingCategory.setName(category.getName());
        existingCategory.setColor(category.getColor());
        existingCategory.setIndex(category.getIndex());
        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
