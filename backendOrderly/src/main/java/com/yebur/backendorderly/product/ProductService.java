package com.yebur.backendorderly.product;

import java.util.List;
import java.util.Optional;

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
        return productRepository.save(mapToEntity(product));
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setCategory(product.getCategory());
        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private Product mapToEntity(ProductRequest req){
        Product product = new Product();
        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setCategory(categoryService.findById(req.getCategoryId()));
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
