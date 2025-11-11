package com.yebur.controller;

import com.yebur.model.request.ProductRequest;
import com.yebur.model.response.CategoryResponse;
import com.yebur.service.CategoryService;
import com.yebur.service.ProductService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.math.BigDecimal;
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

    @FXML
    private VBox dynamicFormVB;

    @FXML
    private Button submitButton;

    private ToggleButton selectedButton;

    private List<CategoryResponse> categories;

    private ColumnConstraints columnConstraints1;

    private ColumnConstraints columnConstraints2;

    private String selectedItem;

    private VBox findItem;
    private Label findItemLabel;
    private TextField findItemTextField;

    private VBox name;
    private Label nameLabel;
    private TextField nameTextField;

    private VBox color;
    private Label colorLabel;
    private ColorPicker colorPicker;

    private VBox index;
    private Label indexLabel;
    private TextField indexTextField;

    private VBox price;
    private Label priceLabel;
    private TextField priceTextField;

    private VBox stock;
    private Label stockLabel;
    private TextField stockTextField;

    private VBox category;
    private Label categoryLabel;
    private ComboBox<CategoryResponse> categoryComboBox;

    private VBox destination;
    private Label destinationLabel;
    private ComboBox<String> destinationComboBox;


    private GridPane gridPane = new GridPane();

    @FXML
    public void initialize() {
        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/dataOperation.css").toExternalForm());

        actionToggleGroup = new ToggleGroup();
        addBtn.setToggleGroup(actionToggleGroup);
        editBtn.setToggleGroup(actionToggleGroup);
        deleteBtn.setToggleGroup(actionToggleGroup);

        addBtn.setSelected(true);
        selectedButton = addBtn;

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

        if (selectedItem != null) {
            handleProductAction();
        }
    }



    public void handleProductAction() {
        try{
            categories = CategoryService.getAllCategories();
        }catch(Exception ex){
            ex.getMessage();
        }

        clearGridPane();

        gridPane.setHgap(15);
        gridPane.setVgap(20);
        gridPane.setPadding(new Insets(20,20, 20,20));
        gridPane.setAlignment(Pos.CENTER);

        switch(this.selectedItem){
            case "product"->{
                showProductOperation();
            }
            case "category"->{
                showCategoryOperation();
            }
        }
        submitButton.setOnAction(event -> {
            handleSubmitButton();
        });
    }

    private void showProductOperation(){
        switch (this.selectedButton.getId()) {
            case "addBtn"->{
                createGridPane();

                gridPane.add(name, 0, 0);
                nameLabel.setText("Nombre del producto:");
                gridPane.add(index, 1, 0);
                gridPane.add(price, 0, 1);
                gridPane.add(stock, 1, 1);
                gridPane.add(category, 0, 2);
                gridPane.add(destination, 1, 2);

                submitButton.setText("Crear producto");
                dynamicFormVB.getChildren().setAll(gridPane);

                applyFormStyles(gridPane);
            }
            case "editBtn"->{
                createGridPane();

                gridPane.add(findItem, 0, 0, 2 ,1);
                findItemLabel.setText("Buscar producto: ");
                gridPane.add(name, 0, 1);
                nameLabel.setText("Nombre del producto:");
                gridPane.add(index, 1, 1);
                gridPane.add(price, 0, 2);
                gridPane.add(stock, 1, 2);
                gridPane.add(category, 0, 3);
                gridPane.add(destination, 1, 3);

                submitButton.setText("Modificar producto");
                dynamicFormVB.getChildren().setAll(gridPane);

                applyFormStyles(gridPane);
            }
            case "deleteBtn"->{
                createGridPane();

                gridPane.add(findItem, 0, 0, 2 ,1);
                findItemLabel.setText("Buscar producto: ");
                gridPane.add(name, 0, 1);
                nameLabel.setText("Nombre del producto:");
                nameTextField.setEditable(false);
                gridPane.add(index, 1, 1);
                indexTextField.setEditable(false);
                gridPane.add(price, 0, 2);
                priceTextField.setEditable(false);
                gridPane.add(stock, 1, 2);
                stockTextField.setEditable(false);
                gridPane.add(category, 0, 3);
                categoryComboBox.setDisable(true);
                gridPane.add(destination, 1, 3);
                destinationComboBox.setDisable(true);

                submitButton.setText("Eliminar producto");
                dynamicFormVB.getChildren().setAll(gridPane);

                applyFormStyles(gridPane);
            }
        }
    }

    private void showCategoryOperation(){
        switch (this.selectedButton.getId()) {
            case "addBtn"->{
                createGridPane();

                gridPane.add(name, 0, 0);
                nameLabel.setText("Nombre de la categoria:");
                gridPane.add(index, 1, 0);
                gridPane.add(color, 0, 1, 2, 1);

                submitButton.setText("Crear categoria");
                dynamicFormVB.getChildren().setAll(gridPane);

                applyFormStyles(gridPane);
            }
            case "editBtn"->{
                createGridPane();

                gridPane.add(findItem, 0, 0, 2 ,1);
                findItemLabel.setText("Buscar categoria: ");
                gridPane.add(name, 0, 1);
                nameLabel.setText("Nombre de la categoria:");
                gridPane.add(index, 1, 1);
                gridPane.add(color, 0, 2, 2, 1);

                submitButton.setText("Modificar categoria");
                dynamicFormVB.getChildren().setAll(gridPane);

                applyFormStyles(gridPane);
            }
            case "deleteBtn"->{
                createGridPane();

                gridPane.add(findItem, 0, 0, 2 ,1);
                findItemLabel.setText("Buscar categoria: ");
                gridPane.add(name, 0, 1);
                nameLabel.setText("Nombre de la categoria:");
                nameTextField.setEditable(false);
                gridPane.add(index, 1, 1);
                indexTextField.setEditable(false);
                gridPane.add(color, 0, 3, 2, 1);
                colorPicker.setDisable(true);

                submitButton.setText("Eliminar categoria");
                dynamicFormVB.getChildren().setAll(gridPane);

                applyFormStyles(gridPane);
            }
        }
    }

    private void createGridPane() {
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.setPadding(new Insets(20,20, 20,20));
        gridPane.setAlignment(Pos.CENTER);

        columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setPercentWidth(50);
        columnConstraints1.setHgrow(Priority.ALWAYS);
        columnConstraints2 = new ColumnConstraints();
        columnConstraints2.setPercentWidth(50);
        columnConstraints2.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(columnConstraints1, columnConstraints1);
        findItem = new VBox(5);
        findItemLabel = new Label();
        findItemTextField = new TextField();
        findItem.getChildren().addAll(findItemLabel, findItemTextField);
        name = new VBox(5);
        nameLabel = new Label();
        nameTextField = new TextField();
        name.getChildren().addAll(nameLabel, nameTextField);

        index = new VBox(5);
        indexLabel = new Label("Index:");
        indexTextField = new TextField();
        index.getChildren().addAll(indexLabel, indexTextField);

        color = new VBox(5);
        colorLabel = new Label("Color:");
        colorPicker = new ColorPicker();
        color.getChildren().addAll(colorLabel, colorPicker);

        price = new VBox(5);
        priceLabel = new Label("Precio:");
        priceTextField = new TextField();
        price.getChildren().addAll(priceLabel, priceTextField);

        stock = new VBox(5);
        stockLabel = new Label("Stock:");
        stockTextField = new TextField();
        stock.getChildren().addAll(stockLabel, stockTextField);

        category = new VBox(5);
        categoryLabel = new Label("Categoria:");
        categoryComboBox =  new ComboBox<>();
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

        destination = new VBox(5);
        destinationLabel = new Label("Destinacion:");
        destinationComboBox =  new ComboBox<>();
        destinationComboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(destinationComboBox, Priority.ALWAYS);
        destinationComboBox.getItems().addAll("Bebidas", "Barra", "Cocina");
        destinationComboBox.setPromptText("Selecciona un destino");
        destination.getChildren().addAll(destinationLabel, destinationComboBox);

    }
    private void applyFormStyles(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label) {
                node.getStyleClass().add("form-label");
            } else if (node instanceof TextField) {
                node.getStyleClass().add("form-textfield");
            } else if (node instanceof ComboBox<?>) {
                node.getStyleClass().add("form-combobox");
            }

            if (node instanceof Parent child) {
                applyFormStyles(child);
            }
        }
    }


    private void clearGridPane() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();
    }

    public void setSelectedItem(String selectedItem) {
        this.selectedItem = selectedItem;

        if (root != null) {
            handleProductAction();
        }
    }

    private void handleSubmitButton(){
        switch(selectedItem){
            case "product" ->{
                switch(selectedButton.getId()){
                    case "addBtn"-> {
                        ProductRequest product = new ProductRequest(
                                nameTextField.getText(),
                                new BigDecimal(priceTextField.getText()),
                                Integer.parseInt(stockTextField.getText()),
                                categoryComboBox.getSelectionModel().getSelectedItem().getId(),
                                destinationComboBox.getSelectionModel().getSelectedItem());
                        try {
                            ProductService.createProduct(product);
                        }catch (Exception ex){
                            System.out.println(ex.getMessage());
                        }
                        for(Node vbox : gridPane.getChildren()){
                            if(vbox instanceof VBox){
                                for(Node child : ((VBox)vbox).getChildren()){
                                    if(child instanceof TextField){
                                        ((TextField) child).setText("");
                                    }
                                    if(child instanceof ComboBox<?>){
                                        ((ComboBox<?>) child).getSelectionModel().clearSelection();
                                    }
                                }
                            }
                        }
                        categoryComboBox.setPromptText("Selecciona una categoria");
                        destinationComboBox.setPromptText("Selecciona un destino");
                    }
                }
            }
        }
    }
}
