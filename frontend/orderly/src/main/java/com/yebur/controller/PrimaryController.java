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

    private void reloadProducts() {
        if (selectedCategoryId == null)
            return;
        loadProductsForCategory(selectedCategoryId, getMaximumProducts());
    }

    private void loadCategories(int slots) {
        categoryBox.getChildren().clear();

        if (allCategories == null || allCategories.isEmpty()) {
            return;
        }

        final int S = Math.max(1, slots); // слоты всего (включая места под стрелки)
        final int N = allCategories.size(); // всего категорий

        // 1) считаем стартовый индекс текущей страницы, учитывая реальную вместимость
        // прошлых страниц
        int start = 0;
        int remaining = N;
        for (int p = 0; p < currentCategoryPage && remaining > 0; p++) {
            int capPrev;
            if (p == 0) { // первая страница: только →
                capPrev = Math.min(S - 1, remaining);
            } else { // промежуточные страницы: ← и →
                capPrev = Math.min(Math.max(S - 2, 1), remaining);
            }
            start += capPrev;
            remaining -= capPrev;
        }

        // если ушли за пределы (например, изменился размер и текущая страница стала
        // "пустой") — шаг назад
        if (start >= N && currentCategoryPage > 0) {
            currentCategoryPage--;
            loadCategories(S);
            return;
        }

        // 2) определяем вместимость текущей страницы и наличие стрелок
        boolean hasPrev = currentCategoryPage > 0;
        int navSlots = hasPrev ? 1 : 0;

        // если нет "→", можем занять все оставшиеся слоты; если "→" нужен — один слот
        // резервируем
        int capIfNoNext = Math.max(S - navSlots, 1); // максимум элементов без →
        int capIfNext = Math.max(capIfNoNext - 1, 1); // максимум элементов с →

        boolean hasNext;
        int capacity; // сколько категорий рисуем на этой странице

        if (remaining > capIfNoNext) {
            // элементов больше, чем поместится без → → нужна кнопка →
            hasNext = true;
            capacity = capIfNext;
        } else {
            hasNext = false;
            capacity = Math.min(remaining, capIfNoNext);
        }

        int end = Math.min(start + capacity, N);
        this.categories = allCategories.subList(start, end);

        // 3) рисуем категории
        for (Category category : this.categories) {
            Button btn = new Button(category.getName());
            btn.getStyleClass().add("category-btn");
            btn.setStyle("-fx-background-color: " +
                    (category.getColor() != null ? category.getColor() : "#f9fafb"));
            btn.setOnAction(e -> {
                selectedCategoryId = category.getId();
                currentProductPage = 0;
                reloadProducts();
            });
            categoryBox.getChildren().add(btn);
        }

        // 4) стрелки навигации
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

    private void loadProductsForCategory(Long categoryId, int pageSize) {
        try {
            productBox.getChildren().clear();

            this.productsByCategory = ProductService.getProductsPageByCategory(categoryId, currentProductPage,
                    pageSize);

            for (Product product : this.productsByCategory) {
                Button btn = new Button(product.getName());
                btn.getStyleClass().add("product-btn");
                productBox.getChildren().add(btn);
            }

            // Кнопка "назад"
            if (currentProductPage > 0) {
                Button prevBtn = new Button("←");
                prevBtn.getStyleClass().add("product-btn");
                prevBtn.setOnAction(e -> {
                    currentProductPage--;
                    reloadProducts();
                });
                productBox.getChildren().add(0, prevBtn);
            }

            // Кнопка "вперёд"
            if (this.productsByCategory.size() == pageSize) {
                Button nextBtn = new Button("→");
                nextBtn.getStyleClass().add("product-btn");
                nextBtn.setOnAction(e -> {
                    currentProductPage++;
                    reloadProducts();
                });
                productBox.getChildren().add(nextBtn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getMaximumCategories() {
        // количество колонок
        int cols = categoryBox.getPrefColumns() > 0 ? categoryBox.getPrefColumns() : 3;

        // фактическая высота контейнера
        double h = categoryBox.getParent().getLayoutBounds().getHeight();

        // параметры кнопок (как в CSS)
        double tileH = 75; // фиксированная высота кнопки
        double vgap = categoryBox.getVgap(); // отступы между строками

        if (h <= 0) {
            return cols * 3; // fallback (например, по умолчанию 9 кнопок)
        }

        // сколько строк помещается
        int rows = (int) Math.floor((h + vgap) / (tileH + vgap));
        rows = Math.max(rows, 1);

        return cols * rows;
    }

    private int getMaximumProducts() {
        int cols = productBox.getPrefColumns() > 0 ? productBox.getPrefColumns() : 3;

        double h = productBox.getBoundsInParent().getHeight(); // тоже лучше через Bounds
        double vgap = productBox.getVgap();
        double tileH = productBox.getPrefTileHeight() > 0 ? productBox.getPrefTileHeight() : 80;

        if (h <= 0)
            return cols * 3;

        int rows = (int) Math.floor((h + vgap) / (tileH + vgap));
        rows = Math.max(rows, 1);
        return cols * rows;
    }
}
