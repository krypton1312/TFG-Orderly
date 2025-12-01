package com.yebur.controller;

import com.yebur.model.request.CategoryRequest;
import com.yebur.model.request.ProductRequest;
import com.yebur.model.request.RestTableRequest;
import com.yebur.model.request.SupplementRequest;
import com.yebur.model.response.*;
import com.yebur.service.CategoryService;
import com.yebur.service.ProductService;
import com.yebur.service.RestTableService;
import com.yebur.service.SupplementService;
import com.yebur.ui.CustomDialog;
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
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.util.List;

import static com.yebur.ui.CustomDialog.showError;

public class DataOperationController {

    // ---------- ENUMS ----------
    private enum EntityType {PRODUCT, CATEGORY, TABLE, SUPPLEMENT}

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
    private VBox findItem, name, color, index, price, stock, category, destination, tablePosition, tableStatus, supplementCategoriesVBox, supplementProductsVBox;
    private HBox tableButtons;
    private Label findItemLabel, nameLabel, colorLabel, indexLabel, priceLabel, stockLabel, categoryLabel, destinationLabel, tablePositionLabel, tableStatusLabel, supplementCategoriesLabel, supplementProductsLabel;
    private TextField findItemTextField, nameTextField, indexTextField, priceTextField, stockTextField, supplementCategoriesTextField, supplementProductsTextField;
    private ColorPicker colorPicker;
    private ComboBox<CategoryResponse> categoryComboBox;
    private ComboBox<String> destinationComboBox;
    private ComboBox<String> tableStatusComboBox;
    private Button tablePositionOutside;
    private Button tablePositionInside;
    private FlowPane selectedCategoriesPane, selectedProductsPane;
    private GridPane gridPane = new GridPane();

    // ---------- LIST SUPPORT ----------
    private ObservableList<?> items;
    private FilteredList<?> filteredItems;
    private ListView<?> listItemsView;

    private boolean anyModificationDone = false;
    private boolean isTableInside;

    private final ObservableList<CategoryResponse> selectedCategoriesForSupplement = FXCollections.observableArrayList();
    private final ObservableList<ProductResponse> selectedProductsForSupplement = FXCollections.observableArrayList();
    private static final String TRASH_SVG =
            "M10 11 V17 " +
                    "M14 11 V17 " +
                    "M19 6 V20 A2 2 0 0 1 17 22 H7 A2 2 0 0 1 5 20 V6 " +
                    "M3 6 H21 " +
                    "M8 6 V4 A2 2 0 0 1 10 2 H14 A2 2 0 0 1 16 4 V6";


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
            case TABLE -> setupTableUI();
            case SUPPLEMENT -> setupSupplementUI();
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

    private void setupTableUI() {
        switch (selectedAction) {
            case ADD -> showTableAddForm();
            case EDIT -> showTableEditForm();
            case DELETE -> showTableDeleteForm();
        }
    }

    private void setupSupplementUI() {
        switch (selectedAction) {
            case ADD -> showSupplementAddForm();
            case EDIT -> showSupplementEditForm();
            //case DELETE -> showSupplementDeleteForm();
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
                    findItemTextField,
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
                    findItemTextField,
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

    private void showTableAddForm(){
        createGridPane();
        gridPane.add(index, 0, 0);
        indexLabel.setText("Numero de la mesa:");
        gridPane.add(tableStatus, 1, 0);
        tableStatusLabel.setText("Estado de la mesa:");
        gridPane.add(tablePosition, 0, 1, 2, 1);
        dynamicFormVB.getChildren().setAll(gridPane);
        submitButton.setText("Crear tabla");
        applyFormStyles(gridPane);
    }

    private void showTableEditForm() {
        createGridPane();
        gridPane.add(findItem, 0, 0, 2, 1);
        findItemLabel.setText("Buscar mesa: ");
        gridPane.add(index, 0, 1);
        indexLabel.setText("Numero del mesa:");
        gridPane.add(tableStatus, 1, 1);
        tableStatusLabel.setText("Estado de la mesa:");
        tablePositionLabel.setText("Posicion de la mesa:");
        gridPane.add(tablePosition, 0, 2, 2, 1);

        submitButton.setText("Modificar mesa");
        dynamicFormVB.getChildren().setAll(gridPane);
        applyFormStyles(gridPane);

        try {
            setupDynamicList(
                    RestTableService.getAllRestTables(),
                    findItemTextField,
                    RestTableResponse::getName,
                    restTable -> {
                        selectedItemPopup = restTable;
                        indexTextField.setText(String.valueOf(restTable.getNumber()));
                        tableStatusComboBox.setValue(restTable.getStatus());
                        if(restTable.getPosition().equals("OUTSIDE")){
                            tablePositionInside.getStyleClass().remove("table-buttons-pressed");
                            tablePositionOutside.getStyleClass().add("table-buttons-pressed");
                            isTableInside = false;
                        }else{
                            tablePositionOutside.getStyleClass().remove("table-buttons-pressed");
                            tablePositionInside.getStyleClass().add("table-buttons-pressed");
                            isTableInside = true;
                        }

                    },
                    true
            );
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void showTableDeleteForm() {
        showTableEditForm();
        nameTextField.setEditable(false);
        indexTextField.setEditable(false);
        tablePositionInside.setDisable(true);
        tablePositionOutside.setDisable(true);
        submitButton.setText("Eliminar mesa");
    }

    private void showSupplementAddForm() {
        createGridPane();

        gridPane.add(name, 0, 0);
        nameLabel.setText("Nombre del suplemento:");
        gridPane.add(price, 1, 0);

        gridPane.add(supplementCategoriesVBox, 0, 1, 2, 1);
        gridPane.add(supplementProductsVBox, 0, 2, 2, 1);
        try {
            setupDynamicList(
                    categories,
                    supplementCategoriesTextField,
                    CategoryResponse::getName,
                    category -> {
                        if (!selectedCategoriesForSupplement.contains(category)) {
                            selectedCategoriesForSupplement.add(category);
                            addCategoryChip(category);
                        }
                        supplementCategoriesTextField.clear();
                    },
                    false
            );
        } catch (Exception e) {
            System.err.println("Error setting up supplement categories list: " + e.getMessage());
        }
        try {
            List<ProductResponse> products = ProductService.getAllProducts();
            setupDynamicList(
                    products,
                    supplementProductsTextField,
                    ProductResponse::getName,
                    product -> {
                        if (!selectedProductsForSupplement.contains(product)) {
                            selectedProductsForSupplement.add(product);
                            addProductChip(product);
                        }
                        supplementProductsTextField.clear();
                    },
                    false
            );
        } catch (Exception e) {
            System.err.println("Error setting up supplement products list: " + e.getMessage());
        }

        submitButton.setText("Crear suplemento");
        dynamicFormVB.getChildren().setAll(gridPane);
        applyFormStyles(gridPane);
    }

    private void showSupplementEditForm() {
        createGridPane();

        // ----- разметка -----
        gridPane.add(findItem, 0, 0, 2, 1);
        findItemLabel.setText("Buscar suplemento:");

        gridPane.add(name, 0, 1);
        nameLabel.setText("Nombre del suplemento:");
        gridPane.add(price, 1, 1);

        gridPane.add(supplementCategoriesVBox, 0, 2, 2, 1);
        gridPane.add(supplementProductsVBox, 0, 3, 2, 1); // 💡 раньше у тебя было (0,2,2,1) и для категорий, и для продуктов

        submitButton.setText("Modificar suplemento");
        dynamicFormVB.getChildren().setAll(gridPane);
        applyFormStyles(gridPane);

        try {
            setupDynamicList(
                    SupplementService.getAllSupplements(),
                    findItemTextField,
                    SupplementResponse::getName,
                    supplement -> {
                        selectedItemPopup = supplement;
                        nameTextField.setText(supplement.getName());
                        priceTextField.setText(String.valueOf(supplement.getPrice()));

                        selectedCategoriesForSupplement.clear();
                        selectedCategoriesPane.getChildren().clear();
                        selectedProductsForSupplement.clear();
                        selectedProductsPane.getChildren().clear();

                        if (supplement.getCategories() != null) {
                            for (CategoryResponseSummary c : supplement.getCategories()) {
                                CategoryResponse cr = new CategoryResponse();
                                cr.setId(c.getId());
                                cr.setName(c.getName());

                                if (!selectedCategoriesForSupplement.contains(cr)) {
                                    selectedCategoriesForSupplement.add(cr);
                                    addCategoryChip(cr);
                                }
                            }
                        }
                        if (supplement.getProducts() != null) {
                            for (ProductResponseSummary p : supplement.getProducts()) {

                                ProductResponse pr = new ProductResponse();
                                pr.setId(p.getId());
                                pr.setName(p.getName());

                                if (!selectedProductsForSupplement.contains(pr)) {
                                    selectedProductsForSupplement.add(pr);
                                    addProductChip(pr);
                                }
                            }
                        }
                    },
                    true
            );
        } catch (Exception e) {
            System.err.println("Error setting up supplement list: " + e.getMessage());
        }

        try {
            setupDynamicList(
                    categories,
                    supplementCategoriesTextField,
                    CategoryResponse::getName,
                    category -> {
                        if (!selectedCategoriesForSupplement.contains(category)) {
                            selectedCategoriesForSupplement.add(category);
                            addCategoryChip(category);
                        }
                        supplementCategoriesTextField.clear();
                    },
                    false
            );
        } catch (Exception e) {
            System.err.println("Error setting up supplement categories list: " + e.getMessage());
        }

        try {
            List<ProductResponse> products = ProductService.getAllProducts();
            setupDynamicList(
                    products,
                    supplementProductsTextField,
                    ProductResponse::getName,
                    product -> {
                        if (!selectedProductsForSupplement.contains(product)) {
                            selectedProductsForSupplement.add(product);
                            addProductChip(product);
                        }
                        supplementProductsTextField.clear();
                    },
                    false
            );
        } catch (Exception e) {
            System.err.println("Error setting up supplement products list: " + e.getMessage());
        }
    }

    // ---------- SUBMIT HANDLER ----------
    private void handleSubmitButton() {
        if (verifyNotBlank()) return;
        try {
            switch (selectedEntity) {
                case PRODUCT -> handleProductSubmit();
                case CATEGORY -> handleCategorySubmit();
                case TABLE -> handleTableSubmit();
                case SUPPLEMENT -> handleSupplementSubmit();
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
        if(anyModificationDone){
            clearFormFields(gridPane);
        }
    }

    private void handleProductSubmit() throws Exception {
        ProductRequest product = buildProductRequest();
        Stage stage = (Stage) submitButton.getScene().getWindow();
        anyModificationDone = false;

        switch (selectedAction) {

            case ADD -> {
                if (confirmDataModification(stage, "Confirme la creación del nuevo elemento")) {
                    ProductService.createProduct(product);
                    anyModificationDone = true;
                }
            }

            case EDIT -> {
                if (selectedItemPopup instanceof ProductResponse p &&
                        confirmDataModification(stage, "Confirme la modificación del producto")) {

                    ProductService.updateProduct(p.getId(), product);
                    anyModificationDone = true;
                }
            }

            case DELETE -> {
                if (selectedItemPopup instanceof ProductResponse p &&
                        confirmDataModification(stage, "¿Está seguro de que desea eliminar este producto?")) {

                    ProductService.deleteProduct(p.getId());
                    anyModificationDone = true;
                }
            }
        }


        refreshEntityList();
    }

    private void handleCategorySubmit() throws Exception {
        CategoryRequest category = buildCategoryRequest();
        Stage stage = (Stage) submitButton.getScene().getWindow();
        anyModificationDone = false;

        switch (selectedAction) {
            case ADD -> {
                if(confirmDataModification(stage, "Confirme creacion de la nueva categoria")){
                    CategoryService.createCategory(category);
                    anyModificationDone = true;
                }
            }
            case EDIT -> {
                if (selectedItemPopup instanceof CategoryResponse p &&
                        confirmDataModification(stage, "Confirme la modificación de la categoria")) {
                    CategoryService.updateCategory(p.getId(), category);
                    anyModificationDone = true;
                }
            }
            case DELETE -> {
                if (selectedItemPopup instanceof CategoryResponse p &&
                        confirmDataModification(stage, "¿Está seguro de que desea eliminar este producto?")) {
                    CategoryService.deleteCategory(p.getId());
                    anyModificationDone = true;
                }
            }
        }

        refreshEntityList();
    }

    private void handleTableSubmit() {
        RestTableRequest table = buildTableRequest();
        Stage stage = (Stage) submitButton.getScene().getWindow();
        anyModificationDone = false;

        switch (selectedAction) {
            case ADD -> {
                if(confirmDataModification(stage, "Confirme creacion de la nueva tabla")){
                    try{
                        RestTableService.createTable(table);
                        anyModificationDone = true;
                    }catch (Exception e){
                        showError(e.getMessage());
                    }
                }
            }
            case EDIT -> {
                if(selectedItemPopup instanceof RestTableResponse rt &&
                        confirmDataModification(stage, "Confirme la modificacion de la tabla")){
                    try{
                        RestTableService.updateTable(rt.getId(),table);
                        anyModificationDone = true;
                    }catch (Exception e){
                        showError(e.getMessage());
                    }
                }
            }
            case DELETE -> {
                if(selectedItemPopup instanceof RestTableResponse rt &&
                        confirmDataModification(stage, "¿Está seguro de que desea eliminar este producto?")){
                    try{
                        RestTableService.deleteTable(rt.getId());
                        anyModificationDone = true;
                    }catch (Exception e){
                        showError(e.getMessage());
                    }
                }
            }
        }

        refreshEntityList();
    }

    private void handleSupplementSubmit() throws Exception {
        SupplementRequest supplement = buildSupplementRequest();
        Stage stage = (Stage) submitButton.getScene().getWindow();
        anyModificationDone = false;
        switch (selectedAction) {
            case ADD -> {
                if(confirmDataModification(stage, "Confirme creacion del nuevo suplemento")){
                    try{
                        System.out.println(supplement);
                        SupplementService.createSupplement(supplement);
                        anyModificationDone = true;
                    }catch (Exception e){
                        showError("El supplemento ya existe");
                    }
                }
            }
            case EDIT -> {
                if (selectedItemPopup instanceof SupplementResponse s &&
                        confirmDataModification(stage, "Confirme la modificación del suplemento")) {
                    SupplementService.updateSupplement(s.getId(), supplement);
                    anyModificationDone = true;
                }
            }
        }
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

    private CategoryRequest buildCategoryRequest() {
        return new CategoryRequest(
                nameTextField.getText(),
                getSelectedColortoHex(colorPicker.getValue()),
                Integer.parseInt(indexTextField.getText())
        );
    }

    private SupplementRequest buildSupplementRequest() {
        List<Long> categoryIds = selectedCategoriesForSupplement.stream()
                .map(CategoryResponse::getId)
                .toList();

        List<Long> productIds = selectedProductsForSupplement.stream()
                .map(ProductResponse::getId)
                .toList();

        return new SupplementRequest(
                nameTextField.getText(),
                new BigDecimal(priceTextField.getText()),
                categoryIds,
                productIds
        );
    }


    private RestTableRequest buildTableRequest() {
        int number = Integer.parseInt(indexTextField.getText());
        return new RestTableRequest(number,tableStatusComboBox.getSelectionModel().getSelectedItem(), isTableInside ? "INSIDE" : "OUTSIDE");
    }

    private void clearFormFields(Pane root) {
        for (Node node : root.getChildren()) {
            if (node instanceof TextField tf) tf.clear();
            else if (node instanceof ComboBox<?> cb) cb.getSelectionModel().clearSelection();
            else if (node instanceof Button button) button.getStyleClass().remove("table-buttons-pressed");
            else if (node instanceof Pane pane) clearFormFields(pane);
        }
    }

    private boolean verifyNotBlank() {
        boolean isAnyElementBlank = false;
        for (Node vbox : gridPane.getChildren()) {
            if (vbox instanceof VBox vb) {
                for (Node child : vb.getChildren()) {
                    if (child instanceof TextField tf && tf.getText().trim().isEmpty()) {
                        if (tf == supplementCategoriesTextField || tf == supplementProductsTextField) {
                            continue;
                        }
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
        destination.setMaxWidth(Double.MAX_VALUE);
        destinationComboBox.getItems().addAll("Bebidas", "Barra", "Cocina");
        destinationComboBox.setPrefWidth(Double.MAX_VALUE);
        destinationComboBox.setPromptText("Selecciona un destino");
        destination.getChildren().addAll(destinationLabel, destinationComboBox);

        tablePosition = new VBox(5);
        tablePositionLabel = new Label("Posiсion:");
        tableButtons = new HBox(5);

        tablePositionInside = new Button("Dentro");
        tablePositionInside.setMaxWidth(Double.MAX_VALUE);
        tablePositionOutside = new Button("Fuera");
        tablePositionOutside.setMaxWidth(Double.MAX_VALUE);
        tableButtons.getChildren().addAll(tablePositionInside, tablePositionOutside);
        tableButtons.setPrefWidth(Double.MAX_VALUE);
        tableButtons.setAlignment(Pos.CENTER);
        HBox.setHgrow(tablePositionInside, Priority.ALWAYS);
        HBox.setHgrow(tablePositionOutside, Priority.ALWAYS);
        tablePosition.getChildren().addAll(tablePositionLabel, tableButtons);

        tableStatus = new VBox(5);
        tableStatusLabel = new Label("Estado:");
        tableStatusComboBox = new ComboBox<>();
        tableStatusComboBox.setMaxWidth(Double.MAX_VALUE);
        tableStatusComboBox.getItems().addAll("Disponible", "Reservado", "Fuera de servicio");
        tableStatus.getChildren().addAll(tableStatusLabel, tableStatusComboBox);

        supplementCategoriesVBox = new VBox(5);
        supplementCategoriesLabel = new Label("Categorias:");
        supplementCategoriesTextField = new TextField();
        selectedCategoriesPane = new FlowPane(5, 5);
        selectedCategoriesPane.setPrefWrapLength(300);
        supplementCategoriesVBox.getChildren().addAll(supplementCategoriesLabel, supplementCategoriesTextField, selectedCategoriesPane);

        supplementProductsVBox = new VBox(5);
        supplementProductsLabel = new Label("Productos:");
        supplementProductsTextField = new TextField();
        selectedProductsPane = new FlowPane(5, 5);
        selectedProductsPane.setPrefWrapLength(300);
        supplementProductsVBox.getChildren().addAll(supplementProductsLabel, supplementProductsTextField, selectedProductsPane);

        addClickHandler(nameTextField);
        addClickHandler(priceTextField);
        addClickHandler(stockTextField);
        addClickHandler(categoryComboBox);
        addClickHandler(indexTextField);
        addClickHandler(destinationComboBox);
        addClickHandler(tableButtons);
        addClickHandler(supplementCategoriesTextField);
        addClickHandler(supplementProductsTextField);
        addTableButtonsHandler(tableButtons);
    }

    private HBox createChip(String text, Runnable onRemove) {
        HBox chip = new HBox(4);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("chip");

        Label label = new Label(text);
        label.getStyleClass().add("chip-text");

        Button removeBtn = new Button();
        removeBtn.getStyleClass().add("chip-remove");
        removeBtn.setFocusTraversable(false);
        removeBtn.setMinSize(16, 16);
        removeBtn.setPrefSize(16, 16);
        removeBtn.setMaxSize(16, 16);
        removeBtn.setPadding(Insets.EMPTY);

        SVGPath icon = new SVGPath();
        icon.setContent(TRASH_SVG);
        icon.setFill(Color.TRANSPARENT);
        icon.setStroke(Color.web("#555555"));
        icon.setStrokeWidth(1.7);
        icon.setScaleX(0.55);
        icon.setScaleY(0.55);

        removeBtn.setGraphic(icon);

        removeBtn.setOnAction(e -> {
            onRemove.run();
        });

        HBox.setMargin(removeBtn, new Insets(0, 2, 0, 4));

        chip.getChildren().addAll(label, removeBtn);
        return chip;
    }

    private void addCategoryChip(CategoryResponse category) {
        final HBox[] chipRef = new HBox[1];

        chipRef[0] = createChip(
                category.getName(),
                () -> {
                    selectedCategoriesForSupplement.remove(category);
                    selectedCategoriesPane.getChildren().remove(chipRef[0]);
                }
        );

        selectedCategoriesPane.getChildren().add(chipRef[0]);
    }

    private void addCategoryChip(CategoryResponseSummary category) {
        final HBox[] chipRef = new HBox[1];

        chipRef[0] = createChip(
                category.getName(),
                () -> {
                    selectedCategoriesForSupplement.remove(category);
                    selectedCategoriesPane.getChildren().remove(chipRef[0]);
                }
        );

        selectedCategoriesPane.getChildren().add(chipRef[0]);
    }


    private void addProductChip(ProductResponse product) {
        final HBox[] chipRef = new HBox[1];

        chipRef[0] = createChip(
                product.getName(),
                () -> {
                    selectedProductsForSupplement.remove(product);
                    selectedProductsPane.getChildren().remove(chipRef[0]);
                }
        );

        selectedProductsPane.getChildren().add(chipRef[0]);
    }

    private void addProductChip(ProductResponseSummary product) {
        final HBox[] chipRef = new HBox[1];

        chipRef[0] = createChip(
                product.getName(),
                () -> {
                    selectedProductsForSupplement.remove(product);
                    selectedProductsPane.getChildren().remove(chipRef[0]);
                }
        );

        selectedProductsPane.getChildren().add(chipRef[0]);
    }

    private void addClickHandler(Node node) {
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> node.getStyleClass().remove("blank-element"));
    }

    private void addTableButtonsHandler(Node node) {
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {

            for (Node item : tableButtons.getChildren()) {
                item.getStyleClass().remove("table-buttons-pressed");
            }

            Node clicked = (Node) e.getTarget();
            while (clicked != null && !(clicked instanceof Button)) {
                clicked = clicked.getParent();
            }

            if (clicked instanceof Button) {
                if(clicked ==  tablePositionOutside) {
                    isTableInside = false;
                }else{
                    isTableInside = true;
                }
                clicked.getStyleClass().add("table-buttons-pressed");
            }
        });
    }


    private void applyFormStyles(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label) node.getStyleClass().add("form-label");
            else if (node instanceof TextField) node.getStyleClass().add("form-textfield");
            else if (node instanceof ComboBox<?>) node.getStyleClass().add("form-combobox");
            else if(node instanceof HBox){
                ((HBox) node).getChildren();
                for(Node child : ((HBox) node).getChildren()){
                    if(child instanceof Button) child.getStyleClass().add("table-buttons");
                }
            }
            if (node instanceof Parent p) applyFormStyles(p);
        }
    }

    // ---------- DYNAMIC LIST ----------
    private <T> void setupDynamicList(
            List<T> data,
            TextField textField,
            java.util.function.Function<T, String> displayTextExtractor,
            java.util.function.Consumer<T> onItemSelected,
            boolean suppressPopup
    ) {
        ObservableList<T> obsList = FXCollections.observableArrayList(data);
        FilteredList<T> filteredList = new FilteredList<>(obsList, s -> true);
        ListView<T> listView = new ListView<>(filteredList);
        listView.setPrefHeight(150);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        textField.widthProperty().addListener((obs, o, n) -> listView.setPrefWidth(n.doubleValue()));

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
            if (textField.getScene() == null) return;
            Bounds b = textField.localToScreen(textField.getBoundsInLocal());
            if (b == null) return;
            listView.setPrefWidth(textField.getWidth());
            if (popup.isShowing()) popup.hide();
            popup.show(textField.getScene().getWindow(), b.getMinX(), b.getMaxY() + 2);
        });

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
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
            if (text != null) textField.setText(text);
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

        textField.focusedProperty().addListener((o, oldV, newV) -> {
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
                                    findItemTextField,
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
                                    findItemTextField,
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
                    case TABLE ->  {
                        if (selectedAction == ActionType.EDIT || selectedAction == ActionType.DELETE) {
                            setupDynamicList(
                                    RestTableService.getAllRestTables(),
                                    findItemTextField,
                                    RestTableResponse::getName,
                                    restTable -> {
                                        selectedItemPopup = restTable;
                                        if(restTable.getPosition().equals("OUTSIDE")){
                                            indexTextField.setText(String.valueOf(restTable.getNumber()));
                                            tablePositionInside.getStyleClass().remove("table-buttons-pressed");
                                            tablePositionOutside.getStyleClass().add("table-buttons-pressed");
                                            isTableInside = false;
                                        }else{
                                            indexTextField.setText(String.valueOf(restTable.getNumber()));
                                            tablePositionOutside.getStyleClass().remove("table-buttons-pressed");
                                            tablePositionInside.getStyleClass().add("table-buttons-pressed");
                                            isTableInside = true;
                                        }
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

    private boolean confirmDataModification(Stage stage, String message) {
        int result = CustomDialog.show(
                stage,
                "Confirmación",
                message,
                "Confirmar",
                "Cancelar",
                null
        );
        return result == 1;
    }
    private String getSelectedColortoHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

}
