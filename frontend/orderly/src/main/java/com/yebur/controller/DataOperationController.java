package com.yebur.controller;

import com.yebur.model.request.ProductRequest;
import com.yebur.model.response.CategoryResponse;
import com.yebur.model.response.ProductResponse;
import com.yebur.service.CategoryService;
import com.yebur.service.ProductService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.util.List;

public class DataOperationController {

    // ---------- ENUMS ----------
    private enum EntityType {PRODUCT, CATEGORY}

    private enum ActionType {ADD, EDIT, DELETE}

    // ---------- FXML ----------
    @FXML
    private ToggleButton addBtn;
    @FXML
    private ToggleButton editBtn;
    @FXML
    private ToggleButton deleteBtn;
    @FXML
    private VBox root;
    @FXML
    private VBox dynamicFormVB;
    @FXML
    private Button submitButton;

    // ---------- TOGGLES ----------
    private ToggleGroup actionToggleGroup;
    private ToggleButton selectedButton;
    private EntityType selectedEntity;
    private ActionType selectedAction;

    // ---------- DATA ----------
    private List<CategoryResponse> categories;
    private Object selectedItemPopup;

    // ---------- UI ELEMENTS ----------
    private VBox findItem, name, color, index, price, stock, category, destination;
    private Label findItemLabel, nameLabel, colorLabel, indexLabel, priceLabel, stockLabel, categoryLabel, destinationLabel;
    private TextField findItemTextField, nameTextField, indexTextField, priceTextField, stockTextField;
    private ColorPicker colorPicker;
    private ComboBox<CategoryResponse> categoryComboBox;
    private ComboBox<String> destinationComboBox;
    private GridPane gridPane = new GridPane();

    // ---------- LIST SUPPORT ----------
    private ObservableList<?> items;
    private FilteredList<?> filteredItems;
    private ListView<?> listItemsView;

    // ---------- INITIALIZATION ----------
    @FXML
    public void initialize() {
        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/dataOperation.css").toExternalForm());

        actionToggleGroup = new ToggleGroup();
        addBtn.setToggleGroup(actionToggleGroup);
        editBtn.setToggleGroup(actionToggleGroup);
        deleteBtn.setToggleGroup(actionToggleGroup);

        selectedButton = addBtn;
        selectedAction = ActionType.ADD;

        actionToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
                return;
            }
            selectedButton = (ToggleButton) newToggle;
            switch (selectedButton.getId()) {
                case "addBtn" -> selectedAction = ActionType.ADD;
                case "editBtn" -> selectedAction = ActionType.EDIT;
                case "deleteBtn" -> selectedAction = ActionType.DELETE;
            }
            handleEntityAction();
        });

        if (selectedEntity != null) {
            handleEntityAction();
        }
    }

    // ---------- ENTITY TYPE SELECTOR ----------
    public void setSelectedItem(String selectedItem) {
        this.selectedEntity = EntityType.valueOf(selectedItem.toUpperCase());
        if (root != null) handleEntityAction();
    }

    // ---------- MAIN ENTITY ACTION ----------
    private void handleEntityAction() {
        try {
            categories = CategoryService.getAllCategories();
        } catch (Exception ex) {
            System.err.println("Error loading categories: " + ex.getMessage());
        }

        clearGridPane();

        gridPane.setHgap(15);
        gridPane.setVgap(20);
        gridPane.setPadding(new Insets(20));
        gridPane.setAlignment(Pos.CENTER);

        switch (selectedEntity) {
            case PRODUCT -> setupProductUI();
            case CATEGORY -> setupCategoryUI();
        }

        submitButton.setOnAction(event -> handleSubmitButton());
    }

    // ---------- PRODUCT UI SETUP ----------
    private void setupProductUI() {
        switch (selectedAction) {
            case ADD -> showProductAddForm();
            case EDIT -> showProductEditForm();
            case DELETE -> showProductDeleteForm();
        }
    }

    // ---------- CATEGORY UI SETUP ----------
    private void setupCategoryUI() {
        switch (selectedAction) {
            case ADD -> showCategoryAddForm();
            case EDIT -> showCategoryEditForm();
            case DELETE -> showCategoryDeleteForm();
        }
    }

    // ---------- PRODUCT FORMS ----------
    private void showProductAddForm() {
        createGridPane();
        gridPane.add(name, 0, 0, 2, 1);
        nameLabel.setText("Nombre del producto:");
        gridPane.add(price, 0, 1);
        gridPane.add(stock, 1, 1);
        gridPane.add(category, 0, 2);
        gridPane.add(destination, 1, 2);

        submitButton.setText("Crear producto");
        dynamicFormVB.getChildren().setAll(gridPane);
        applyFormStyles(gridPane);
    }

    private void showProductEditForm() {
        createGridPane();
        gridPane.add(findItem, 0, 0, 2, 1);
        findItemLabel.setText("Buscar producto: ");
        gridPane.add(name, 0, 1, 2, 1);
        nameLabel.setText("Nombre del producto:");
        gridPane.add(price, 0, 2);
        gridPane.add(stock, 1, 2);
        gridPane.add(category, 0, 3);
        gridPane.add(destination, 1, 3);

        submitButton.setText("Modificar producto");
        dynamicFormVB.getChildren().setAll(gridPane);
        applyFormStyles(gridPane);

        try {
            setupDynamicList(
                    ProductService.getAllProducts(),
                    ProductResponse::getName,
                    product -> {
                        selectedItemPopup = product;
                        nameTextField.setText(product.getName());
                        priceTextField.setText(product.getPrice().toString());
                        stockTextField.setText(String.valueOf(product.getStock()));
                        destinationComboBox.getSelectionModel().select(product.getDestination());
                        categoryComboBox.getSelectionModel().select(
                                categories.stream()
                                        .filter(c -> c.getId().equals(product.getCategoryId()))
                                        .findFirst()
                                        .orElse(null)
                        );
                    },
                    true
            );
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void showProductDeleteForm() {
        showProductEditForm();
        nameTextField.setEditable(false);
        priceTextField.setEditable(false);
        stockTextField.setEditable(false);
        categoryComboBox.setDisable(true);
        destinationComboBox.setDisable(true);
        submitButton.setText("Eliminar producto");
    }

    // ---------- CATEGORY FORMS ----------
    private void showCategoryAddForm() {
        createGridPane();
        gridPane.add(name, 0, 0);
        nameLabel.setText("Nombre de la categoria:");
        gridPane.add(index, 1, 0);
        gridPane.add(color, 0, 1, 2, 1);

        submitButton.setText("Crear categoria");
        dynamicFormVB.getChildren().setAll(gridPane);
        applyFormStyles(gridPane);
    }

    private void showCategoryEditForm() {
        createGridPane();
        gridPane.add(findItem, 0, 0, 2, 1);
        findItemLabel.setText("Buscar categoria: ");
        gridPane.add(name, 0, 1);
        nameLabel.setText("Nombre de la categoria:");
        gridPane.add(index, 1, 1);
        gridPane.add(color, 0, 2, 2, 1);

        submitButton.setText("Modificar categoria");
        dynamicFormVB.getChildren().setAll(gridPane);
        applyFormStyles(gridPane);

        try {
            setupDynamicList(
                    CategoryService.getAllCategories(),
                    CategoryResponse::getName,
                    category -> {
                        selectedItemPopup = category;
                        nameTextField.setText(category.getName());
                        indexTextField.setText(String.valueOf(category.getIndex()));
                        try {
                            colorPicker.setValue(Color.web(category.getColor()));
                        } catch (Exception ignored) {
                        }
                    },
                    true
            );
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void showCategoryDeleteForm() {
        showCategoryEditForm();
        nameTextField.setEditable(false);
        indexTextField.setEditable(false);
        colorPicker.setDisable(true);
        submitButton.setText("Eliminar categoria");
    }

    // ---------- SUBMIT HANDLER ----------
    private void handleSubmitButton() {
        if (verifyNotBlank()) return;
        try {
            switch (selectedEntity) {
                case PRODUCT -> handleProductSubmit();
                case CATEGORY -> handleCategorySubmit();
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
        clearFormFields(gridPane);
    }

    private void handleProductSubmit() throws Exception {
        ProductRequest product = buildProductRequest();
        System.out.println(product.getDestination());
        switch (selectedAction) {
            case ADD -> ProductService.createProduct(product);
            case EDIT -> {
                if (selectedItemPopup instanceof ProductResponse p)
                    ProductService.updateProduct(p.getId(), product);
            }
            case DELETE -> {
                if (selectedItemPopup instanceof ProductResponse p)
                    ProductService.deleteProduct(p.getId());
            }
        }

        refreshEntityList();
    }

    private void handleCategorySubmit() throws Exception {
        switch (selectedAction) {
            case ADD -> System.out.println("Создать категорию");
            case EDIT -> System.out.println("Редактировать категорию");
            case DELETE -> System.out.println("Удалить категорию");
        }

        refreshEntityList();
    }

    // ---------- HELPERS ----------
    private ProductRequest buildProductRequest() {
        return new ProductRequest(
                nameTextField.getText(),
                new BigDecimal(priceTextField.getText()),
                Integer.parseInt(stockTextField.getText()),
                categoryComboBox.getSelectionModel().getSelectedItem().getId(),
                destinationComboBox.getSelectionModel().getSelectedItem()
        );
    }

    private void clearFormFields(Pane root) {
        for (Node node : root.getChildren()) {
            if (node instanceof TextField tf) tf.clear();
            else if (node instanceof ComboBox<?> cb) cb.getSelectionModel().clearSelection();
            else if (node instanceof Pane pane) clearFormFields(pane);
        }
    }

    private boolean verifyNotBlank() {
        boolean isAnyElementBlank = false;
        for (Node vbox : gridPane.getChildren()) {
            if (vbox instanceof VBox vb) {
                for (Node child : vb.getChildren()) {
                    if (child instanceof TextField tf && tf.getText().trim().isEmpty()) {
                        child.getStyleClass().add("blank-element");
                        isAnyElementBlank = true;
                    } else if (child instanceof ComboBox<?> cb && cb.getSelectionModel().getSelectedItem() == null) {
                        child.getStyleClass().add("blank-element");
                        isAnyElementBlank = true;
                    }
                }
            }
        }
        return isAnyElementBlank;
    }

    private void clearGridPane() {
        if (items != null) ((ObservableList<?>) items).clear();
        if (filteredItems != null) ((FilteredList<?>) filteredItems).setPredicate(x -> false);
        if (listItemsView != null) ((ListView<?>) listItemsView).getItems().clear();
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();
    }

    // ---------- CREATE GRID ----------
    private void createGridPane() {
        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        gridPane.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridPane.getColumnConstraints().addAll(col1, col2);

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
        priceTextField.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*(\\.\\d{0,2})?") ? c : null));
        price.getChildren().addAll(priceLabel, priceTextField);

        stock = new VBox(5);
        stockLabel = new Label("Stock:");
        stockTextField = new TextField();
        stockTextField.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
        stock.getChildren().addAll(stockLabel, stockTextField);

        category = new VBox(5);
        categoryLabel = new Label("Categoria:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(categoryComboBox, Priority.ALWAYS);
        categoryComboBox.getItems().addAll(categories);
        categoryComboBox.setPromptText("Selecciona una categoria");

        categoryComboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CategoryResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        categoryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CategoryResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        category.getChildren().addAll(categoryLabel, categoryComboBox);


        destination = new VBox(5);
        destinationLabel = new Label("Destinacion:");
        destinationComboBox = new ComboBox<>();
        destinationComboBox.getItems().addAll("Bebidas", "Barra", "Cocina");
        destinationComboBox.setPromptText("Selecciona un destino");
        destination.getChildren().addAll(destinationLabel, destinationComboBox);

        addClickHandler(nameTextField);
        addClickHandler(priceTextField);
        addClickHandler(stockTextField);
        addClickHandler(categoryComboBox);
        addClickHandler(indexTextField);
        addClickHandler(destinationComboBox);
    }

    private void addClickHandler(Node node) {
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> node.getStyleClass().remove("blank-element"));
    }

    private void applyFormStyles(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label) node.getStyleClass().add("form-label");
            else if (node instanceof TextField) node.getStyleClass().add("form-textfield");
            else if (node instanceof ComboBox<?>) node.getStyleClass().add("form-combobox");
            if (node instanceof Parent p) applyFormStyles(p);
        }
    }

    // ---------- DYNAMIC LIST ----------
    private <T> void setupDynamicList(
            List<T> data,
            java.util.function.Function<T, String> displayTextExtractor,
            java.util.function.Consumer<T> onItemSelected,
            boolean suppressPopup
    ) {
        ObservableList<T> obsList = FXCollections.observableArrayList(data);
        FilteredList<T> filteredList = new FilteredList<>(obsList, s -> true);
        ListView<T> listView = new ListView<>(filteredList);
        listView.setPrefHeight(150);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        findItemTextField.widthProperty().addListener((obs, o, n) -> listView.setPrefWidth(n.doubleValue()));

        listView.setStyle("""
        -fx-background-color: white;
        -fx-border-color: #ccc;
        -fx-border-radius: 6;
        -fx-background-radius: 6;
        -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);
    """);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayTextExtractor.apply(item));
            }
        });

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.getContent().add(listView);

        final boolean[] suppressFilter = {false};

        Runnable showPopup = () -> Platform.runLater(() -> {
            if (findItemTextField.getScene() == null) return;
            Bounds b = findItemTextField.localToScreen(findItemTextField.getBoundsInLocal());
            if (b == null) return;
            listView.setPrefWidth(findItemTextField.getWidth());
            if (popup.isShowing()) popup.hide();
            popup.show(findItemTextField.getScene().getWindow(), b.getMinX(), b.getMaxY() + 2);
        });

        findItemTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressFilter[0]) return;
            // 🔹 Если обновляем список после submit — не показываем popup автоматически
            if (suppressPopup && !popup.isShowing()) return;

            if (newVal == null || newVal.isEmpty()) {
                filteredList.setPredicate(i -> true);
            } else {
                filteredList.setPredicate(i -> {
                    String text = displayTextExtractor.apply(i);
                    return text != null && text.toLowerCase().contains(newVal.toLowerCase());
                });
            }

            listView.getSelectionModel().clearSelection();
            if (!filteredList.isEmpty()) showPopup.run();
            else popup.hide();
        });

        listView.getSelectionModel().selectedItemProperty().addListener((o, oldSel, newSel) -> {
            if (newSel == null) return;
            suppressFilter[0] = true;

            String text = displayTextExtractor.apply(newSel);
            if (text != null) findItemTextField.setText(text);
            submitButton.requestFocus();

            Platform.runLater(() -> {
                if (popup.isShowing()) popup.hide();
                onItemSelected.accept(newSel);
                listView.getSelectionModel().clearSelection();

                PauseTransition d = new PauseTransition(Duration.millis(150));
                d.setOnFinished(e -> suppressFilter[0] = false);
                d.play();
            });
        });

        findItemTextField.focusedProperty().addListener((o, oldV, newV) -> {
            if (newV) {
                if (findItemTextField.getText() == null || findItemTextField.getText().isEmpty()) {
                    filteredList.setPredicate(i -> true);
                    if (!filteredList.isEmpty()) showPopup.run();
                }
            } else {
                popup.hide();
            }
        });

        this.items = obsList;
        this.filteredItems = filteredList;
        this.listItemsView = listView;
    }

    // ---------- REFRESH ENTITY LIST ----------
    private void refreshEntityList() {
        Platform.runLater(() -> {
            try {
                switch (selectedEntity) {
                    case PRODUCT -> {
                        if (selectedAction == ActionType.EDIT || selectedAction == ActionType.DELETE) {
                            setupDynamicList(
                                    ProductService.getAllProducts(),
                                    ProductResponse::getName,
                                    product -> {
                                        selectedItemPopup = product;
                                        nameTextField.setText(product.getName());
                                        priceTextField.setText(product.getPrice().toString());
                                        stockTextField.setText(String.valueOf(product.getStock()));
                                        destinationComboBox.getSelectionModel().select(product.getDestination());
                                        categoryComboBox.getSelectionModel().select(
                                                categories.stream()
                                                        .filter(c -> c.getId().equals(product.getCategoryId()))
                                                        .findFirst()
                                                        .orElse(null)
                                        );
                                    },
                                    false
                            );
                        }
                    }
                    case CATEGORY -> {
                        if (selectedAction == ActionType.EDIT || selectedAction == ActionType.DELETE) {
                            setupDynamicList(
                                    CategoryService.getAllCategories(),
                                    CategoryResponse::getName,
                                    category -> {
                                        selectedItemPopup = category;
                                        nameTextField.setText(category.getName());
                                        indexTextField.setText(String.valueOf(category.getIndex()));
                                        try {
                                            colorPicker.setValue(Color.web(category.getColor()));
                                        } catch (Exception ignored) {}
                                    },
                                    false
                            );
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });
    }

}
