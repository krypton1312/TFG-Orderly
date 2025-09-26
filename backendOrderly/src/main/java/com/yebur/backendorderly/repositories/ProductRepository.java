package com.yebur.backendorderly.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yebur.backendorderly.dto.output.ProductResponse;
import com.yebur.backendorderly.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Override
    List<Product> findAll();

    @Query("SELECT new com.yebur.backendorderly.dto.output.ProductResponse(p.id, p.name, p.price, p.stock, p.category.id) FROM Product p")
    List<ProductResponse> findAllProductDTO();

    @Override
    Optional<Product> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.dto.output.ProductResponse(p.id, p.name, p.price, p.stock, p.category.id) FROM Product p WHERE p.id = :id")
    Optional<ProductResponse> findProductDTOById(Long id);

    @Query("SELECT new com.yebur.backendorderly.dto.output.ProductResponse(p.id, p.name, p.price, p.stock, p.category.id) FROM Product p WHERE p.category.id = :categoryId")
    List<ProductResponse> findProductDTOByCategoryId(Long categoryId);
    
}