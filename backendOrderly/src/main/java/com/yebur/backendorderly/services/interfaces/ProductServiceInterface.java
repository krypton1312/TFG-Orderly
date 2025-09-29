package com.yebur.backendorderly.services.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yebur.backendorderly.dto.output.ProductResponse;
import com.yebur.backendorderly.entities.Product;

public interface ProductServiceInterface {

    List<Product> findAll();

    List<ProductResponse> findAllProductDTO();

    Page<ProductResponse> findAllProductDTOPage(Long categoryId, Pageable pageable);

    Optional<Product> findById(Long id);

    Optional<ProductResponse> findProductDTOById(Long id);

    List<ProductResponse> findProductDTOByCategoryId(Long categoryId);

    Product createProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);
    
} 
