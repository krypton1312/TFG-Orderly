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

    private List<Category> allCategories;
    private List<Product> allProducts;
    private List<Category> categories;
    private List<Product> productsByCategory;

    @FXML
    private VBox orderItems;

    @FXML
    private TextField displayField;

    private int currentCategoryPage = 0;
    private int currentProductPage = 0;
    private Long selectedCategoryId;
    private int categoryPageSize;
    private int productPageSize;

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            try {
                this.allCategories = CategoryService.getAllCategories();
            } catch (Exception e) {
                e.printStackTrace();
            }
            reloadCategories();
        });
    }

    private void reloadCategories() {
        if (categoryPageSize <= 0) {
            categoryPageSize = getMaximumCategories();
        }
        loadCategories(categoryPageSize);
    }

    private void reloadProducts(String color) {
        if (productPageSize <= 0) {
            productPageSize = getMaximumProducts();
        }
        if (selectedCategoryId == null)
            return;
        loadProductsForCategory(productPageSize, selectedCategoryId, color);
    }

    private void loadCategories(int slots) {
        categoryBox.getChildren().clear();

        if (allCategories == null || allCategories.isEmpty()) {
            return;
        }

        final int S = Math.max(1, slots);
        final int N = allCategories.size();
        int start = 0;
        int remaining = N;
        for (int p = 0; p < currentCategoryPage && remaining > 0; p++) {
            int capPrev;
            if (p == 0) {
                capPrev = Math.min(S - 1, remaining);
            } else {
                capPrev = Math.min(Math.max(S - 2, 1), remaining);
            }
            start += capPrev;
            remaining -= capPrev;
        }

        if (start >= N && currentCategoryPage > 0) {
            currentCategoryPage--;
            reloadCategories();
            return;
        }

        boolean hasPrev = currentProductPage > 0;
        int navSlots = hasPrev ? 1 : 0;

        int capIfNoNext = Math.max(S - navSlots, 1);
        int capIfNext = Math.max(capIfNoNext - 1, 1);

        boolean hasNext;
        int capacity;

        if (remaining > capIfNoNext) {
            hasNext = true;
            capacity = capIfNext;
        } else {
            hasNext = false;
            capacity = Math.min(remaining, capIfNoNext);
        }

        int end = Math.min(start + capacity, N);
        this.categories = allCategories.subList(start, end);

        for (Category category : this.categories) {
            Button btn = new Button(category.getName());
            btn.getStyleClass().add("category-btn");
            btn.setStyle("-fx-background-color: " +
                    (category.getColor() != null ? category.getColor() : "#f9fafb"));
            btn.setOnAction(e -> {
                selectedCategoryId = category.getId();
                currentProductPage = 0;
                reloadProducts(category.getColor());
            });
            categoryBox.getChildren().add(btn);
        }
        if (hasPrev) {
            Button prevBtn = new Button("←");
            prevBtn.getStyleClass().add("category-btn");
            prevBtn.setOnAction(e -> {
                currentCategoryPage--;
                loadCategories(S);
            });
            categoryBox.getChildren().add(0, prevBtn);
        }

        if (hasNext) {
            Button nextBtn = new Button("→");
            nextBtn.getStyleClass().add("category-btn");
            nextBtn.setOnAction(e -> {
                currentCategoryPage++;
                loadCategories(S);
            });
            categoryBox.getChildren().add(nextBtn);
        }
    }

    private void loadProductsForCategory(int slots, Long selectedCategoryId, String color) {
        productBox.getChildren().clear();

        try {
            this.allProducts = ProductService.getProductsByCategory(selectedCategoryId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (allProducts == null || allProducts.isEmpty()) {
            return;
        }

        final int S = Math.max(1, slots);
        final int N = allProducts.size();

        int start = 0;
        int remaining = N;
        for (int p = 0; p < currentProductPage && remaining > 0; p++) {
            int capPrev;
            if (p == 0) {
                capPrev = Math.min(S - 1, remaining);
            } else {
                capPrev = Math.min(Math.max(S - 2, 1), remaining);
            }
            start += capPrev;
            remaining -= capPrev;
        }

        if (start >= N && currentProductPage > 0) {
            currentProductPage--;
            loadProductsForCategory(S, selectedCategoryId, color);
            return;
        }

        boolean hasPrev = currentProductPage > 0;
        int navSlots = hasPrev ? 1 : 0;

        int capIfNoNext = Math.max(S - navSlots, 1);
        int capIfNext = Math.max(capIfNoNext - 1, 1);

        boolean hasNext;
        int capacity;

        if (remaining > capIfNoNext) {
            hasNext = true;
            capacity = capIfNext;
        } else {
            hasNext = false;
            capacity = Math.min(remaining, capIfNoNext);
        }

        int end = Math.min(start + capacity, N);
        this.productsByCategory = allProducts.subList(start, end);

        if (hasPrev) {
            Button prevBtn = new Button("←");
            prevBtn.getStyleClass().add("product-btn");
            prevBtn.setOnAction(e -> {
                currentProductPage--;
                loadProductsForCategory(S, selectedCategoryId, color);
            });
            productBox.getChildren().add(prevBtn);
        }

        for (Product product : this.productsByCategory) {
            Button btn = new Button(product.getName());
            btn.getStyleClass().add("product-btn");
            btn.setStyle("-fx-background-color: " + (color != null ? color : "#f9fafb"));
            btn.setOnAction(e -> {
                System.out.println("Product clicked: " + product.getName());
            });
            productBox.getChildren().add(btn);
        }
-
        if (hasNext) {
            if (!productBox.getChildren().isEmpty()) {
                productBox.getChildren().remove(productBox.getChildren().size() - 1);
            }

            Button nextBtn = new Button("→");
            nextBtn.getStyleClass().add("product-btn");
            nextBtn.setOnAction(e -> {
                currentProductPage++;
                loadProductsForCategory(S, selectedCategoryId, color);
            });
            productBox.getChildren().add(nextBtn);
        }
    }

    private int getMaximumCategories() {
        int cols = categoryBox.getPrefColumns() > 0 ? categoryBox.getPrefColumns() : 3;
        double h = categoryBox.getParent().getLayoutBounds().getHeight();
        double tileH = 75;
        double vgap = categoryBox.getVgap();

        if (h <= 0) {
            return cols * 3;
        }

        int rows = (int) Math.floor((h + vgap) / (tileH + vgap));
        rows = Math.max(rows, 1);

        return cols * rows;
    }

    private int getMaximumProducts() {
        int cols = productBox.getPrefColumns() > 0 ? productBox.getPrefColumns() : 5;

        double h = productBox.getLayoutBounds().getHeight();

        double tileH;
        if (!productBox.getChildren().isEmpty()) {
            tileH = productBox.getChildren().get(0).getLayoutBounds().getHeight();
        } else {
            tileH = 60;
        }

        double vgap = productBox.getVgap();

        if (h <= 0) {
            return cols * 3;
        }
        
        int rows = (int) Math.floor((h + vgap * 0.9) / (tileH + vgap));
        rows = Math.max(rows, 1);

        return cols * rows;
    }

}
