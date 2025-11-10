package com.yebur.controller;

import com.yebur.model.response.CategoryResponse;
import com.yebur.service.CategoryService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class DataOperationController {

    @FXML
    private ToggleButton addBtn;

    @FXML
    private ToggleButton editBtn;

    @FXML
    private ToggleButton deleteBtn;

    private ToggleGroup actionToggleGroup;

    @FXML
    private VBox root;

    @FXML
    private VBox mainContainerVB;

    private ToggleButton selectedButton;

    private List<CategoryResponse> categories;

    private GridPane gridPane = new GridPane();
    @FXML
    public void initialize() {
        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/dataOperation.css").toExternalForm());
        actionToggleGroup = new ToggleGroup();
        addBtn.setToggleGroup(actionToggleGroup);
        editBtn.setToggleGroup(actionToggleGroup);
        deleteBtn.setToggleGroup(actionToggleGroup);

        addBtn.setSelected(true);

        actionToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
                return;
            }

            if (newToggle != null && newToggle != oldToggle) {
                selectedButton = (ToggleButton) newToggle;
                handleProductAction();
            }
        });
        selectedButton = addBtn;
        handleProductAction();
    }


    public void handleProductAction() {
        try{
            categories = CategoryService.getAllCategories();
        }catch(Exception ex){
            ex.getMessage();
        }
        if(gridPane.getChildren().isEmpty()){
            gridPane.getChildren().clear();
        }
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10,0,10,0));
        gridPane.setAlignment(Pos.CENTER);

        switch (this.selectedButton.getId()) {
            case "addBtn"->{
                ColumnConstraints columnConstraints1 = new ColumnConstraints();
                columnConstraints1.setPercentWidth(50);
                columnConstraints1.setHgrow(Priority.ALWAYS);
                ColumnConstraints columnConstraints2 = new ColumnConstraints();
                columnConstraints2.setPercentWidth(50);
                columnConstraints2.setHgrow(Priority.ALWAYS);
                gridPane.getColumnConstraints().addAll(columnConstraints1, columnConstraints1);

                VBox name = new VBox(5);
                Label nameLabel = new Label("Nombre del producto:");
                TextField nameTextField = new TextField();
                name.getChildren().addAll(nameLabel, nameTextField);
                gridPane.add(name, 0, 0, 2, 1);

                VBox price = new VBox(5);
                Label priceLabel = new Label("Precio:");
                TextField priceTextField = new TextField();
                price.getChildren().addAll(priceLabel, priceTextField);
                gridPane.add(price, 0, 1);

                VBox stock = new VBox(5);
                Label stockLabel = new Label("Stock:");
                TextField stockTextField = new TextField();
                stock.getChildren().addAll(stockLabel, stockTextField);
                gridPane.add(stock, 1, 1);

                VBox category = new VBox(5);
                Label categoryLabel = new Label("Categoria:");
                ComboBox<CategoryResponse> categoryComboBox =  new ComboBox<>();
                categoryComboBox.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(categoryComboBox, Priority.ALWAYS);
                categoryComboBox.getItems().addAll(categories);
                categoryComboBox.setPromptText("Selecciona una categoria");
                categoryComboBox.setCellFactory(list -> new ListCell<>() {
                    @Override protected void updateItem(CategoryResponse item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                });
                categoryComboBox.setButtonCell(new ListCell<>() {
                    @Override protected void updateItem(CategoryResponse item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                });
                category.getChildren().addAll(categoryLabel, categoryComboBox);
                gridPane.add(category, 0, 2);

                VBox destination = new VBox(5);
                Label destinationLabel = new Label("Destinacion:");
                ComboBox<String> destinationComboBox =  new ComboBox<>();
                destinationComboBox.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(destinationComboBox, Priority.ALWAYS);
                destinationComboBox.getItems().addAll("Bebidas", "Barra", "Cocina");
                destinationComboBox.setPromptText("Selecciona un destino");
                destination.getChildren().addAll(destinationLabel, destinationComboBox);
                gridPane.add(destination, 1, 2);

                mainContainerVB.getChildren().add(gridPane);
            }
        }
    }
}
