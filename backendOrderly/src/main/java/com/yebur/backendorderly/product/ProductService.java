package com.yebur.backendorderly.product;

import java.util.List;
import java.util.Optional;

import com.yebur.backendorderly.category.Category;
import com.yebur.backendorderly.category.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service("productService")
public class ProductService implements ProductServiceInterface {
    
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    @Override
    public List<ProductResponse> findAllProductDTO() {
        return productRepository.findAllProductDTO();
    }
    @Override
    public Page<ProductResponse> findAllProductDTOPage(Long categoryId, Pageable pageable){
        return productRepository.findAllProductDTOPage(categoryId, pageable);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<ProductResponse> findProductDTOById(Long id) {
        return productRepository.findProductDTOById(id);
    }

    @Override
    public List<ProductResponse> findProductDTOByCategoryId(Long categoryId) {
        return productRepository.findProductDTOByCategoryId(categoryId);
    }

    @Override
    public ProductResponse createProduct(ProductRequest product) {
        Product newProduct = productRepository.save(mapToEntity(product));
        return findProductDTOById(newProduct.getId()).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest req) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));

        existingProduct.setName(req.getName());
        existingProduct.setPrice(req.getPrice());
        existingProduct.setStock(req.getStock());
        Category category = categoryService.findById(req.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found with id " + req.getCategoryId()));
        existingProduct.setCategory(category);

        return findProductDTOById(productRepository.save(existingProduct).getId()).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private Product mapToEntity(ProductRequest req){
        Product product = new Product();
        Category category = categoryService.findById(req.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found with id " + req.getCategoryId()));
        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setCategory(category);
        product.setDestination(mapProductDestination(req.getDestination()));
        return product;
    }

    private ProductDestination mapProductDestination(String destination){
        switch (destination){
            case "Bebidas" ->{
                return ProductDestination.DRINKS;
            }
            case "Barra" ->{
                return ProductDestination.BAR;
            }
            case "Cocina" ->{
                return ProductDestination.KITCHEN;
            }
        }
        return null;
    }
}
