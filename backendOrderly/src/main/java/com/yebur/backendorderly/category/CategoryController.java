package com.yebur.backendorderly.category;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/categories")
public class CategoryController {


    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(){
        return ResponseEntity.ok(categoryService.findAllCategoryDTO());
    }

    @GetMapping("/page/{page},{pageSize}")
    public ResponseEntity<List<CategoryResponse>> listPageable(@PathVariable Integer page, @PathVariable Integer pageSize){
        try{
            PageRequest pageable = PageRequest.of(page, pageSize);
            List<CategoryResponse> result = categoryService.findAllCategoryDTOPage(pageable).getContent();
            return ResponseEntity.ok(result);
        }catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/id/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id){
        Optional<CategoryResponse> optional = categoryService.findCategoryDTOById(id);
        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        } else {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "Category is not found with this id: " + id));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequest category, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody CategoryRequest category, BindingResult result, @PathVariable Long id){
        if(result.hasErrors()){
            return validation(result);
        }

        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        Optional<Category> category = categoryService.findById(id);

        if(category.isPresent()){
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
    
    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), "El campo: " + error.getField() + " " + error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
