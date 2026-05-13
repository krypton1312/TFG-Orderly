package com.yebur.controller;

import com.yebur.model.response.CategoryResponse;
import com.yebur.model.response.SupplementResponse;
import com.yebur.service.CategoryService;
import com.yebur.service.SupplementService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.List;

public class CategoriasListController {

    @FXML private BorderPane root;
    @FXML private TableView<CategoryResponse> categoriasTable;
    @FXML private TableColumn<CategoryResponse, String> catIdCol;
    @FXML private TableColumn<CategoryResponse, String> catNameCol;
    @FXML private TableColumn<CategoryResponse, String> catIndexCol;
    @FXML private TableView<SupplementResponse> suplementosTable;
    @FXML private TableColumn<SupplementResponse, String> supIdCol;
    @FXML private TableColumn<SupplementResponse, String> supNameCol;
    @FXML private TableColumn<SupplementResponse, String> supPriceCol;

    private final ObservableList<CategoryResponse> catItems = FXCollections.observableArrayList();
    private final ObservableList<SupplementResponse> supItems = FXCollections.observableArrayList();

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

        catIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        catNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getName()));
        catIndexCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getIndex() != null ? String.valueOf(c.getValue().getIndex()) : "—"));
        categoriasTable.setItems(catItems);

        supIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        supNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getName()));
        supPriceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPrice() != null ? moneyFmt.format(c.getValue().getPrice()) + " €" : "—"));
        suplementosTable.setItems(supItems);

        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<CategoryResponse> cats = CategoryService.getAllCategories();
                List<SupplementResponse> sups = SupplementService.getAllSupplements();
                Platform.runLater(() -> {
                    catItems.setAll(cats);
                    supItems.setAll(sups);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onClose() {
        ((Stage) root.getScene().getWindow()).close();
    }
}
