package com.yebur.backendorderly.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductServiceInterface {

    List<Product> findAll();

    List<ProductResponse> findAllProductDTO();

    Page<ProductResponse> findAllProductDTOPage(Long categoryId, Pageable pageable);

    Optional<Product> findById(Long id);

    Optional<ProductResponse> findProductDTOById(Long id);

    List<ProductResponse> findProductDTOByCategoryId(Long categoryId);

    ProductResponse createProduct(ProductRequest product);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);
    
} 
