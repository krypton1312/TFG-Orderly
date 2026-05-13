package com.yebur.controller;

import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.service.OrderDetailService;
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
import java.util.Map;

public class PedidoDetailController {

    @FXML private BorderPane root;
    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label mesaLabel;
    @FXML private Label empleadoLabel;
    @FXML private Label totalLabel;
    @FXML private TableView<OrderDetailResponse> linesTable;
    @FXML private TableColumn<OrderDetailResponse, String> lineNameCol;
    @FXML private TableColumn<OrderDetailResponse, String> lineAmountCol;
    @FXML private TableColumn<OrderDetailResponse, String> lineUnitPriceCol;
    @FXML private TableColumn<OrderDetailResponse, String> lineTotalCol;
    @FXML private TableColumn<OrderDetailResponse, String> lineStatusCol;
    @FXML private TableColumn<OrderDetailResponse, String> lineCommentCol;

    private final ObservableList<OrderDetailResponse> lines = FXCollections.observableArrayList();
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
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

        lineNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getName() != null ? c.getValue().getName() : "—"));
        lineAmountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getAmount())));
        lineUnitPriceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getUnitPrice() != null ? moneyFmt.format(c.getValue().getUnitPrice()) + " €" : "—"));
        lineTotalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getUnitPrice() != null
                        ? moneyFmt.format(c.getValue().getUnitPrice().multiply(
                                java.math.BigDecimal.valueOf(c.getValue().getAmount()))) + " €"
                        : "—"));
        lineStatusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                toLineStatusLabel(c.getValue().getStatus())));
        lineCommentCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getComment() != null ? c.getValue().getComment() : ""));

        linesTable.setItems(lines);
    }

    private String toLineStatusLabel(String s) {
        if (s == null) return "—";
        return switch (s.toUpperCase()) {
            case "PENDING"     -> "Pendiente";
            case "SENT"        -> "Enviado";
            case "IN_PROGRESS" -> "En curso";
            case "SERVED"      -> "Servido";
            case "PAID"        -> "Pagado";
            default -> s;
        };
    }

    public void populate(OrderResponse order, Map<Long, String> employeeNameMap) {
        titleLabel.setText("DETALLE: PEDIDO #" + order.getId());
        dateLabel.setText(order.getDatetime() != null ? order.getDatetime().format(dtFmt) : "—");
        mesaLabel.setText(order.getRestTable() != null
                ? "Mesa " + order.getRestTable().getNumber() : "—");
        empleadoLabel.setText(employeeNameMap != null
                ? employeeNameMap.getOrDefault(order.getIdEmployee(), "—") : "—");
        totalLabel.setText(moneyFmt.format(order.getTotal()) + " €");

        Thread t = new Thread(() -> {
            try {
                List<OrderDetailResponse> details =
                        OrderDetailService.getAllOrderDetailsByOrderId(order.getId());
                Platform.runLater(() -> lines.setAll(details));
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
