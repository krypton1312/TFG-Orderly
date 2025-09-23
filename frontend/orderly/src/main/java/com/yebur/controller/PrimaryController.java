package com.yebur.controller;

import java.util.List;

import com.yebur.model.Category;
import com.yebur.service.CategoryService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class PrimaryController {

    @FXML
    private VBox categoryBox;

    @FXML
    public void initialize() {
        loadCategoryButtons();
    }

    private void loadCategoryButtons() {
        try {
            List<Category> categories = CategoryService.getAllCategories();
            categoryBox.getChildren().clear();

            for (Category category : categories) {
                Button btn = new Button(category.getName());
                btn.setMinWidth(50);
                btn.setOnAction(e -> System.out.println("Выбрана категория: " + category.getName()));

                categoryBox.getChildren().add(btn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
