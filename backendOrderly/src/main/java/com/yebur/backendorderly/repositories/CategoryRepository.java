package com.yebur.backendorderly.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yebur.backendorderly.dto.output.CategoryResponse;
import com.yebur.backendorderly.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{

    @Override
    List<Category> findAll();

    @Query("SELECT new com.yebur.backendorderly.dto.output.CategoryResponse(c.id, c.name, c.color, c.index) FROM Category c")
    List<CategoryResponse> findAllCategoryDTO();
    
    @Override
    Optional<Category> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.dto.output.CategoryResponse(c.id, c.name, c.color, c.index) FROM Category c WHERE c.id = :id")
    Optional<CategoryResponse> findCategoryDTOById(Long id);
} 
