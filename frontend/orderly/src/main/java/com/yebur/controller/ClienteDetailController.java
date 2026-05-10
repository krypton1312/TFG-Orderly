package com.yebur.controller;

import com.yebur.model.response.ClientResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.service.OrderService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ClienteDetailController {

    @FXML private BorderPane root;
    @FXML private Label titleLabel;
    @FXML private Label nameLabel;
    @FXML private Label orderCountLabel;
    @FXML private TableView<OrderResponse> ordersTable;
    @FXML private TableColumn<OrderResponse, String> orderIdCol;
    @FXML private TableColumn<OrderResponse, String> orderDateCol;
    @FXML private TableColumn<OrderResponse, String> orderMesaCol;
    @FXML private TableColumn<OrderResponse, String> orderTotalCol;
    @FXML private TableColumn<OrderResponse, String> orderEstadoCol;

    private final ObservableList<OrderResponse> orderItems = FXCollections.observableArrayList();
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");

    @FXML
    public void initialize() {
        root.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null) newScene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE)
                    ((Stage) newScene.getWindow()).close();
            });
        });

        orderIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        orderDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDatetime() != null ? c.getValue().getDatetime().format(dtFmt) : "—"));
        orderMesaCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getRestTable() != null ? String.valueOf(c.getValue().getRestTable().getNumber()) : "—"));
        orderTotalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                moneyFmt.format(c.getValue().getTotal()) + " €"));
        orderEstadoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getState() != null ? c.getValue().getState() : "—"));

        ordersTable.setItems(orderItems);
    }

    public void populate(ClientResponse client) {
        titleLabel.setText("DETALLE: CLIENTE #" + client.getId());
        nameLabel.setText(client.getName() != null ? client.getName() : "—");
        orderCountLabel.setText(String.valueOf(client.getOrderCount()));

        Thread t = new Thread(() -> {
            try {
                List<OrderResponse> all = OrderService.getAllOrders();
                List<OrderResponse> clientOrders = all.stream()
                        .filter(o -> o.getIdClient() != null && o.getIdClient().equals(client.getId()))
                        .collect(Collectors.toList());
                Platform.runLater(() -> orderItems.setAll(clientOrders));
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
