package com.yebur.controller;

import java.util.List;

import com.yebur.model.Category;
import com.yebur.model.Product;
import com.yebur.service.CategoryService;
import com.yebur.service.ProductService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class PrimaryController {

    @FXML
    private TilePane categoryBox;

    @FXML
    private TilePane productBox;

    private List<Category> categories;

    private List<Product> productsByCategory;

    @FXML
    private VBox orderItems;

    @FXML
    private TextField displayField;

    @FXML
    public void initialize() {
        loadCategoryButtons();
    }

    private void loadCategoryButtons() {
        try {
            categories = CategoryService.getAllCategories();
            categoryBox.getChildren().clear();

            for (Category category : categories) {
                Button btn = new Button(category.getName());
                btn.getStyleClass().add("category-btn");
                btn.setMinWidth(50);
                btn.setStyle("-fx-background-color: " + category.getColor());
                btn.setOnAction(e -> loadProductsForCategory(category.getId()));
                categoryBox.getChildren().add(btn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductsForCategory(Long categoryId) {
        try {
            productBox.getChildren().clear();
            for (Category category : categories) {
                if (category.getId() == categoryId) {
                    productsByCategory = ProductService.getProductsByCategory(categoryId);
                    for (Product product : productsByCategory) {
                        Button btn = new Button(product.getName());
                        btn.getStyleClass().add("product-btn");
                        btn.setMinWidth(100);
                        productBox.getChildren().add(btn);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
