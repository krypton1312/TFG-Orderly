package com.yebur.controller;

import com.yebur.model.response.ClientResponse;
import com.yebur.service.ClientService;
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

import java.util.List;

public class ClientesListController {

    @FXML private BorderPane root;
    @FXML private TextField searchField;
    @FXML private TableView<ClientResponse> clientesTable;
    @FXML private TableColumn<ClientResponse, String> idCol;
    @FXML private TableColumn<ClientResponse, String> nameCol;
    @FXML private TableColumn<ClientResponse, String> orderCountCol;

    private final ObservableList<ClientResponse> allItems = FXCollections.observableArrayList();
    private FilteredList<ClientResponse> filtered;

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
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getName() != null ? c.getValue().getName() : "—"));
        orderCountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getOrderCount())));

        filtered = new FilteredList<>(allItems, p -> true);
        SortedList<ClientResponse> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(clientesTable.comparatorProperty());
        clientesTable.setItems(sorted);

        searchField.textProperty().addListener((obs, o, val) -> {
            String q = val != null ? val.trim().toLowerCase() : "";
            filtered.setPredicate(c -> q.isEmpty()
                    || (c.getName() != null && c.getName().toLowerCase().contains(q)));
        });

        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<ClientResponse> clients = ClientService.getAllClients();
                Platform.runLater(() -> allItems.setAll(clients));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onClear() {
        searchField.clear();
        filtered.setPredicate(p -> true);
    }

    @FXML
    private void onRowClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ClientResponse selected = clientesTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            openClienteDetail(selected);
        }
    }

    private void openClienteDetail(ClientResponse client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/clienteDetail.fxml"));
            Parent detailRoot = loader.load();
            ClienteDetailController ctrl = loader.getController();
            ctrl.populate(client);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(clientesTable.getScene().getWindow());
            stage.setScene(new Scene(detailRoot));
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
