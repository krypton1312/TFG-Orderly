package com.yebur.backendorderly.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.yebur.backendorderly.dto.output.CategoryResponse;
import com.yebur.backendorderly.entities.Category;
import com.yebur.backendorderly.services.CategoryService;

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
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id){
        Optional<CategoryResponse> optional = categoryService.findCategoryDTOById(id);
        if(optional.isPresent()){
            return ResponseEntity.ok(optional.orElseThrow());
        } else {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "Category is not found with this id: " + id));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Category caterogy, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(caterogy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Category category, BindingResult result, @PathVariable Long id){
        if(result.hasErrors()){
            return validation(result);
        }
        
        Category updatedCategory = categoryService.updateCategory(id, category);
        Optional<Category> categoryOptional = Optional.ofNullable(updatedCategory);

        if(categoryOptional.isPresent()){
            return ResponseEntity.ok(categoryOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
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
