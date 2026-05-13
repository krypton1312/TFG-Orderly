package com.yebur.controller;

import com.yebur.model.response.CategoryResponse;
import com.yebur.model.response.ProductResponse;
import com.yebur.service.CategoryService;
import com.yebur.service.ProductService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductosListController {

    @FXML private BorderPane root;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoriaPicker;
    @FXML private TableView<ProductResponse> productosTable;
    @FXML private TableColumn<ProductResponse, String> idCol;
    @FXML private TableColumn<ProductResponse, String> nameCol;
    @FXML private TableColumn<ProductResponse, String> priceCol;
    @FXML private TableColumn<ProductResponse, String> stockCol;
    @FXML private TableColumn<ProductResponse, String> categoriaCol;
    @FXML private TableColumn<ProductResponse, String> destinationCol;

    private final ObservableList<ProductResponse> allItems = FXCollections.observableArrayList();
    private FilteredList<ProductResponse> filtered;
    private final Map<Long, String> categoryNameMap = new HashMap<>();
    private final ObservableList<String> categoryNames = FXCollections.observableArrayList();

    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");

    @FXML
    public void initialize() {
        root.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene == null) return;
            newScene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE)
                    ((Stage) newScene.getWindow()).close();
            });
            String url = getClass().getResource("/com/yebur/portal/portal-dark.css").toExternalForm();
            Runnable sync = () -> {
                boolean dark = newScene.getStylesheets().stream().anyMatch(s -> s.contains("portal-dark"));
                if (dark) { if (!root.getStylesheets().contains(url)) root.getStylesheets().add(url); }
                else root.getStylesheets().remove(url);
            };
            sync.run();
            newScene.getStylesheets().addListener((javafx.collections.ListChangeListener<String>) c -> sync.run());
        });

        categoriaPicker.setItems(categoryNames);
        categoriaPicker.setValue("Todas");

        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getName() != null ? c.getValue().getName() : "—"));
        priceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPrice() != null ? moneyFmt.format(c.getValue().getPrice()) + " €" : "—"));
        stockCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStock() != null ? String.valueOf(c.getValue().getStock()) : "—"));
        categoriaCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                categoryNameMap.getOrDefault(c.getValue().getCategoryId(), "—")));
        destinationCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDestination() != null ? c.getValue().getDestination() : "—"));

        filtered = new FilteredList<>(allItems, p -> true);
        SortedList<ProductResponse> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(productosTable.comparatorProperty());
        productosTable.setItems(sorted);

        searchField.textProperty().addListener((obs, o, val) -> applyFilters());
        categoriaPicker.valueProperty().addListener((obs, o, val) -> applyFilters());

        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<CategoryResponse> cats = CategoryService.getAllCategories();
                cats.forEach(c -> categoryNameMap.put(c.getId(), c.getName()));
                List<String> names = cats.stream().map(CategoryResponse::getName).toList();
                List<ProductResponse> products = ProductService.getAllProducts();
                Platform.runLater(() -> {
                    categoryNames.setAll("Todas");
                    categoryNames.addAll(names);
                    categoriaPicker.setValue("Todas");
                    allItems.setAll(products);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void applyFilters() {
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String cat = categoriaPicker.getValue();
        filtered.setPredicate(p -> {
            if (!q.isEmpty() && (p.getName() == null || !p.getName().toLowerCase().contains(q)))
                return false;
            if (cat != null && !"Todas".equals(cat)) {
                String pCat = categoryNameMap.getOrDefault(p.getCategoryId(), "");
                if (!cat.equals(pCat)) return false;
            }
            return true;
        });
    }

    @FXML
    private void onClear() {
        searchField.clear();
        categoriaPicker.setValue("Todas");
        filtered.setPredicate(p -> true);
    }

    @FXML
    private void onRowClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ProductResponse selected = productosTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            openProductoDetail(selected);
        }
    }

    private void openProductoDetail(ProductResponse product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/productoDetail.fxml"));
            Parent detailRoot = loader.load();
            ProductoDetailController ctrl = loader.getController();
            ctrl.populate(product, categoryNameMap);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(productosTable.getScene().getWindow());
            Scene scene = new Scene(detailRoot);
            com.yebur.ui.ThemeSupport.copyTheme(detailRoot, scene, productosTable.getScene());
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClose() {
        ((Stage) root.getScene().getWindow()).close();
    }
}
