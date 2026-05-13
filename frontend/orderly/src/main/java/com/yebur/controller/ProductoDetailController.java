package com.yebur.controller;

import com.yebur.model.response.ProductResponse;
import com.yebur.model.response.SupplementResponse;
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
import java.util.Map;
import java.util.stream.Collectors;

public class ProductoDetailController {

    @FXML private BorderPane root;
    @FXML private Label titleLabel;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label categoriaLabel;
    @FXML private Label destinationLabel;
    @FXML private Label noSupsLabel;
    @FXML private TableView<SupplementResponse> suplementosTable;
    @FXML private TableColumn<SupplementResponse, String> supIdCol;
    @FXML private TableColumn<SupplementResponse, String> supNameCol;
    @FXML private TableColumn<SupplementResponse, String> supPriceCol;

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

        supIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        supNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getName() != null ? c.getValue().getName() : "—"));
        supPriceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPrice() != null ? moneyFmt.format(c.getValue().getPrice()) + " €" : "—"));

        suplementosTable.setItems(supItems);
    }

    public void populate(ProductResponse product, Map<Long, String> categoryNameMap) {
        titleLabel.setText("DETALLE: " + (product.getName() != null ? product.getName().toUpperCase() : "PRODUCTO"));
        nameLabel.setText(product.getName() != null ? product.getName() : "—");
        priceLabel.setText(product.getPrice() != null ? moneyFmt.format(product.getPrice()) + " €" : "—");
        categoriaLabel.setText(categoryNameMap != null
                ? categoryNameMap.getOrDefault(product.getCategoryId(), "—") : "—");
        destinationLabel.setText(product.getDestination() != null ? product.getDestination() : "—");

        Thread t = new Thread(() -> {
            try {
                List<SupplementResponse> all = SupplementService.getAllSupplements();
                List<SupplementResponse> forProduct = all.stream()
                        .filter(s -> s.getProducts() != null
                                && s.getProducts().contains(product.getId()))
                        .collect(Collectors.toList());
                Platform.runLater(() -> {
                    supItems.setAll(forProduct);
                    boolean none = forProduct.isEmpty();
                    noSupsLabel.setVisible(none);
                    noSupsLabel.setManaged(none);
                    suplementosTable.setVisible(!none);
                    suplementosTable.setManaged(!none);
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
