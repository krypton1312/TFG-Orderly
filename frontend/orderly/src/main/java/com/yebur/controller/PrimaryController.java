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

    private int currentCategoryPage = 0;
    private int pageSize;

    @FXML
    public void initialize() {
        categoryBox.widthProperty().addListener((obs, oldVal, newVal) -> reloadCategories());
        categoryBox.heightProperty().addListener((obs, oldVal, newVal) -> reloadCategories());

        javafx.application.Platform.runLater(this::reloadCategories);
    }

    private void reloadCategories() {
        pageSize = getMaximumCategories() - 1;
        loadCategories(pageSize);
    }

    private void loadCategories(int pageSize) {
        try {
            categoryBox.getChildren().clear();

            this.categories = CategoryService.getAllCategoriesPage(currentCategoryPage, pageSize);

            for (Category category : this.categories) {
                Button btn = new Button(category.getName());
                btn.getStyleClass().add("category-btn");
                btn.setStyle("-fx-background-color: " +
                        (category.getColor() != null ? category.getColor() : "#f9fafb"));
                btn.setOnAction(e -> loadProductsForCategory(category.getId()));
                categoryBox.getChildren().add(btn);
            }

            if (currentCategoryPage > 0) {
                Button prevBtn = new Button("←");
                prevBtn.getStyleClass().add("category-btn");
                prevBtn.setOnAction(e -> {
                    currentCategoryPage--;
                    reloadCategories();
                });
                categoryBox.getChildren().add(0, prevBtn);
            }

            if (this.categories.size() == pageSize) {
                Button nextBtn = new Button("→");
                nextBtn.getStyleClass().add("category-btn");
                nextBtn.setOnAction(e -> {
                    currentCategoryPage++;
                    reloadCategories();
                });
                categoryBox.getChildren().add(nextBtn);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductsForCategory(Long categoryId) {
        try {
            productBox.getChildren().clear();

            Category cat = this.categories.stream()
                    .filter(c -> c.getId() != null && c.getId().equals(categoryId))
                    .findFirst().orElse(null);
            if (cat == null) return;

            this.productsByCategory = ProductService.getProductsByCategory(categoryId);

            for (Product product : productsByCategory) {
                Button btn = new Button(product.getName());
                btn.getStyleClass().add("product-btn");
                productBox.getChildren().add(btn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getMaximumCategories() {
        int cols = categoryBox.getPrefColumns() > 0 ? categoryBox.getPrefColumns() : 3;

        double h = categoryBox.getHeight();
        double vgap = categoryBox.getVgap();
        double tileH = categoryBox.getPrefTileHeight() > 0 ? categoryBox.getPrefTileHeight() : 80;

        if (h <= 0) return cols * 3;

        int rows = (int) Math.floor((h + vgap) / (tileH + vgap));
        rows = Math.max(rows, 1);
        return cols * rows;
    }
}
