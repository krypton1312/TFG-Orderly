package com.yebur.controller;

import com.yebur.model.response.EmployeeResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.service.EmployeeService;
import com.yebur.service.OrderService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagosListController {

    @FXML private BorderPane root;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> metodoPicker;
    @FXML private TableView<OrderResponse> pagosTable;
    @FXML private TableColumn<OrderResponse, String> idCol;
    @FXML private TableColumn<OrderResponse, String> dateCol;
    @FXML private TableColumn<OrderResponse, String> mesaCol;
    @FXML private TableColumn<OrderResponse, String> totalCol;
    @FXML private TableColumn<OrderResponse, String> metodoPagoCol;
    @FXML private TableColumn<OrderResponse, String> empleadoCol;

    private final ObservableList<OrderResponse> allItems = FXCollections.observableArrayList();
    private FilteredList<OrderResponse> filtered;
    private final Map<Long, String> employeeNameMap = new HashMap<>();

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

        metodoPicker.setItems(FXCollections.observableArrayList("Todos", "Efectivo", "Tarjeta"));
        metodoPicker.setValue("Todos");

        com.yebur.ui.DatePickerStyler.apply(fromDatePicker);
        com.yebur.ui.DatePickerStyler.apply(toDatePicker);

        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDatetime() != null ? c.getValue().getDatetime().format(dtFmt) : "—"));
        mesaCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getRestTable() != null ? String.valueOf(c.getValue().getRestTable().getNumber()) : "—"));
        totalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                moneyFmt.format(c.getValue().getTotal()) + " €"));
        metodoPagoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                toMetodoLabel(c.getValue().getPaymentMethod())));
        empleadoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                employeeNameMap.getOrDefault(c.getValue().getIdEmployee(), "—")));

        filtered = new FilteredList<>(allItems, p -> true);
        SortedList<OrderResponse> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(pagosTable.comparatorProperty());
        pagosTable.setItems(sorted);

        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<EmployeeResponse> employees = EmployeeService.getAllEmployees();
                employees.forEach(e -> employeeNameMap.put(e.getId(),
                        (e.getName() != null ? e.getName() : "") + " " + (e.getLastname() != null ? e.getLastname() : "")));
                List<OrderResponse> orders = OrderService.getAllOrders();
                List<OrderResponse> paid = orders.stream()
                        .filter(o -> "PAID".equals(o.getState()))
                        .toList();
                Platform.runLater(() -> allItems.setAll(paid));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private String toMetodoLabel(String s) {
        if (s == null) return "—";
        return switch (s.toUpperCase()) {
            case "CASH" -> "Efectivo";
            case "CARD" -> "Tarjeta";
            default -> s;
        };
    }

    @FXML
    private void onFilter() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String metodo = metodoPicker.getValue();
        filtered.setPredicate(o -> {
            if (from != null && o.getDatetime() != null && o.getDatetime().toLocalDate().isBefore(from))
                return false;
            if (to != null && o.getDatetime() != null && o.getDatetime().toLocalDate().isAfter(to))
                return false;
            if (metodo != null && !"Todos".equals(metodo)) {
                String rawMetodo = "Efectivo".equals(metodo) ? "CASH" : "CARD";
                if (!rawMetodo.equals(o.getPaymentMethod())) return false;
            }
            return true;
        });
    }

    @FXML
    private void onClear() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        metodoPicker.setValue("Todos");
        filtered.setPredicate(p -> true);
    }

    @FXML
    private void onRowClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            OrderResponse selected = pagosTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            openPedidoDetail(selected);
        }
    }

    void openPedidoDetail(OrderResponse order) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/pedidoDetail.fxml"));
            Parent detailRoot = loader.load();
            PedidoDetailController ctrl = loader.getController();
            ctrl.populate(order, employeeNameMap);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(pagosTable.getScene().getWindow());
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
