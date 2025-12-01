package com.yebur.backendorderly.supplements;

import com.yebur.backendorderly.category.Category;
import com.yebur.backendorderly.category.CategoryService;
import com.yebur.backendorderly.product.Product;
import com.yebur.backendorderly.product.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("supplementService")
public class SupplementService implements SupplementServiceInterface{

    private final SupplementRepository supplementRepository;
    private final CategoryService categoryService;
    private final ProductService productService;

    public SupplementService(SupplementRepository supplementRepository,  CategoryService categoryService, ProductService productService) {
        this.supplementRepository = supplementRepository;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @Override
    public Optional<Supplement> findSupplementById(Long id){
        return supplementRepository.findById(id);
    }

    @Override
    public Optional<SupplementResponse> findSupplementDTOById(Long id){
        return Optional.ofNullable(mapEntityToResponse(findSupplementById(id).orElseThrow(() -> new RuntimeException("Supplement not found"))));
    }

    @Override
    public List<SupplementResponse> findAllSupplementsDTO(){
        return supplementRepository.findAll().stream().map(this::mapEntityToResponse).toList();
    }

    public List<SupplementResponse> findSupplementsByCategory(Long id){
        return supplementRepository.findSupplementByCategoriesId(id).stream().map(this::mapEntityToResponse).toList();
    }

    public SupplementResponse createSupplement(SupplementRequest supplement){
        return mapEntityToResponse(supplementRepository.save(mapRequestToEntity(supplement)));
    }

    @Override
    public SupplementResponse updateSupplement(Long id, SupplementRequest supplement) {
        Supplement toUpdate = findSupplementById(id).orElseThrow(() -> new RuntimeException("Supplement not found"));
        toUpdate.setName(supplement.getName());
        toUpdate.setPrice(supplement.getPrice());
        toUpdate.setCategories(categoryService.findAllByIds(supplement.getCategories_ids()));
        toUpdate.setProducts(productService.findByIds(supplement.getProducts_ids()));
        return mapEntityToResponse(supplementRepository.save(toUpdate));
    }

    @Override
    public void deleteSupplement(Long id) {
        supplementRepository.deleteById(id);
    }

    public Supplement mapRequestToEntity(SupplementRequest supplementRequest){
        Supplement  supplement = new Supplement();
        supplement.setName(supplementRequest.getName());
        supplement.setPrice(supplementRequest.getPrice());
        supplement.setCategories(categoryService.findAllByIds(supplementRequest.getCategories_ids()));
        supplement.setProducts(productService.findByIds(supplementRequest.getProducts_ids()));
        return supplement;
    }

    public SupplementResponse mapEntityToResponse(Supplement supplement) {

        SupplementResponse supplementResponse = new SupplementResponse();
        supplementResponse.setId(supplement.getId());
        supplementResponse.setName(supplement.getName());
        supplementResponse.setPrice(supplement.getPrice());

        supplementResponse.setCategories(
                supplement.getCategories()
                        .stream()
                        .map(c -> new CategoryResponseSummary(c.getId(), c.getName()))
                        .toList()
        );

        supplementResponse.setProducts(
                supplement.getProducts()
                        .stream()
                        .map(p -> new ProductResponseSummary(p.getId(), p.getName()))
                        .toList()
        );

        return supplementResponse;
    }


}
