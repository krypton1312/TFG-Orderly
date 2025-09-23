package com.yebur.backendorderly.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yebur.backendorderly.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{

    @Override
    List<Category> findAll();
    
    @Override
    Optional<Category> findById(Long id);
} 
