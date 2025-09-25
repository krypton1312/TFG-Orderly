package com.yebur.backendorderly.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yebur.backendorderly.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Override
    List<Product> findAll();

    @Override
    Optional<Product> findById(Long id);

    List<Product> findByCategoryId(Long categoryId);
    
}