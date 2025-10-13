package com.yebur.backendorderly.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Override
    List<Product> findAll();

    @Query("SELECT new com.yebur.backendorderly.product.ProductResponse(p.id, p.name, p.price, p.stock, p.category.id) FROM Product p")
    List<ProductResponse> findAllProductDTO();

    @Query("SELECT new com.yebur.backendorderly.product.ProductResponse(p.id, p.name, p.price, p.stock, p.category.id) FROM Product p WHERE p.category.id = :categoryId")
    Page<ProductResponse> findAllProductDTOPage(Long categoryId, Pageable pageable);
    
    @Override
    Optional<Product> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.product.ProductResponse(p.id, p.name, p.price, p.stock, p.category.id) FROM Product p WHERE p.id = :id")
    Optional<ProductResponse> findProductDTOById(Long id);

    @Query("SELECT new com.yebur.backendorderly.product.ProductResponse(p.id, p.name, p.price, p.stock, p.category.id) FROM Product p WHERE p.category.id = :categoryId")
    List<ProductResponse> findProductDTOByCategoryId(Long categoryId);
    
}