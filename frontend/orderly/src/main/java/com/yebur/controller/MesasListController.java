package com.yebur.controller;

import com.yebur.model.response.RestTableResponse;
import com.yebur.service.RestTableService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class MesasListController {

    @FXML private BorderPane root;
    @FXML private TableView<RestTableResponse> mesasTable;
    @FXML private TableColumn<RestTableResponse, String> idCol;
    @FXML private TableColumn<RestTableResponse, String> numberCol;
    @FXML private TableColumn<RestTableResponse, String> nameCol;
    @FXML private TableColumn<RestTableResponse, String> zonaCol;
    @FXML private TableColumn<RestTableResponse, String> statusCol;

    private final ObservableList<RestTableResponse> allItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        root.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null) newScene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE)
                    ((Stage) newScene.getWindow()).close();
            });
        });

        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        numberCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getNumber())));
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getName() != null ? c.getValue().getName() : "—"));
        zonaCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                toZonaLabel(c.getValue().getPosition())));
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                toStatusLabel(c.getValue().getStatus())));

        SortedList<RestTableResponse> sorted = new SortedList<>(allItems);
        sorted.comparatorProperty().bind(mesasTable.comparatorProperty());
        mesasTable.setItems(sorted);

        loadData();
    }

    private String toZonaLabel(String position) {
        if (position == null) return "—";
        return switch (position.toUpperCase()) {
            case "INSIDE" -> "Interior";
            case "OUTSIDE" -> "Exterior";
            default -> position;
        };
    }

    private String toStatusLabel(String status) {
        if (status == null) return "—";
        return switch (status.toUpperCase()) {
            case "AVAILABLE" -> "Disponible";
            case "OCCUPIED" -> "Ocupada";
            default -> status;
        };
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<RestTableResponse> tables = RestTableService.getAllRestTables();
                Platform.runLater(() -> allItems.setAll(tables));
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
