package com.yebur.backendorderly.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{

    @Override
    List<Category> findAll();

    @Query("SELECT new com.yebur.backendorderly.category.CategoryResponse(c.id, c.name, c.color, c.index) FROM Category c ORDER BY c.index ASC")
    List<CategoryResponse> findAllCategoryDTO();

    @Query("SELECT new com.yebur.backendorderly.category.CategoryResponse(c.id, c.name, c.color, c.index) FROM Category c ORDER BY c.index ASC")
    Page<CategoryResponse> findAllCategoryDTOPage(Pageable pageable);
    
    @Override
    Optional<Category> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.category.CategoryResponse(c.id, c.name, c.color, c.index) FROM Category c WHERE c.id = :id ORDER BY c.index ASC")
    Optional<CategoryResponse> findCategoryDTOById(Long id);
} 
