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
    public List<Category> findAllByIds(List<Long> ids){
        return categoryRepository.findByIdIn(ids);
    }
    @Override
    public CategoryResponse createCategory(CategoryRequest category) {
        Category save = categoryRepository.save(mapToEntity(category));
        return findCategoryDTOById(save.getId()).orElseThrow(() -> new RuntimeException("Category Not Found"));
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id " + id));

        existingCategory.setName(category.getName());
        existingCategory.setColor(category.getColor());
        existingCategory.setIndex(category.getIndex());
        categoryRepository.save(existingCategory);
        return findCategoryDTOById(existingCategory.getId()).orElseThrow(() -> new RuntimeException("Category Not Found Id: " + existingCategory.getId()));
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    private Category mapToEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setColor(request.getColor());
        category.setIndex(request.getIndex());
        return category;
    }
}

