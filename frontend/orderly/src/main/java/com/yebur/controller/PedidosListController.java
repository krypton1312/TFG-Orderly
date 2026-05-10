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

public class PedidosListController {

    @FXML private BorderPane root;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> metodoPicker;
    @FXML private ComboBox<String> empleadoPicker;
    @FXML private TextField searchField;
    @FXML private TableView<OrderResponse> pedidosTable;
    @FXML private TableColumn<OrderResponse, String> idCol;
    @FXML private TableColumn<OrderResponse, String> dateCol;
    @FXML private TableColumn<OrderResponse, String> mesaCol;
    @FXML private TableColumn<OrderResponse, String> estadoCol;
    @FXML private TableColumn<OrderResponse, String> totalCol;
    @FXML private TableColumn<OrderResponse, String> metodoPagoCol;
    @FXML private TableColumn<OrderResponse, String> empleadoCol;

    private final ObservableList<OrderResponse> allItems = FXCollections.observableArrayList();
    private FilteredList<OrderResponse> filtered;
    private final Map<Long, String> employeeNameMap = new HashMap<>();
    private final ObservableList<String> employeeNames = FXCollections.observableArrayList();

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

        metodoPicker.setItems(FXCollections.observableArrayList("Todos", "CASH", "CARD"));
        metodoPicker.setValue("Todos");

        empleadoPicker.setItems(employeeNames);
        empleadoPicker.setValue("Todos");

        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDatetime() != null ? c.getValue().getDatetime().format(dtFmt) : "—"));
        mesaCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getRestTable() != null ? String.valueOf(c.getValue().getRestTable().getNumber()) : "—"));
        estadoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getState() != null ? c.getValue().getState() : "—"));
        totalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                moneyFmt.format(c.getValue().getTotal()) + " €"));
        metodoPagoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPaymentMethod() != null ? c.getValue().getPaymentMethod() : "—"));
        empleadoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                employeeNameMap.getOrDefault(c.getValue().getIdEmployee(), "—")));

        filtered = new FilteredList<>(allItems, p -> true);
        SortedList<OrderResponse> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(pedidosTable.comparatorProperty());
        pedidosTable.setItems(sorted);

        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<EmployeeResponse> employees = EmployeeService.getAllEmployees();
                employees.forEach(e -> employeeNameMap.put(e.getId(),
                        (e.getName() != null ? e.getName() : "") + " " + (e.getLastname() != null ? e.getLastname() : "")));
                List<String> names = employees.stream()
                        .map(e -> (e.getName() != null ? e.getName() : "") + " " + (e.getLastname() != null ? e.getLastname() : ""))
                        .toList();
                List<OrderResponse> orders = OrderService.getAllOrders();
                Platform.runLater(() -> {
                    employeeNames.setAll("Todos");
                    employeeNames.addAll(names);
                    allItems.setAll(orders);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onFilter() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String metodo = metodoPicker.getValue();
        String empleado = empleadoPicker.getValue();
        String search = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

        filtered.setPredicate(o -> {
            if (from != null && o.getDatetime() != null && o.getDatetime().toLocalDate().isBefore(from))
                return false;
            if (to != null && o.getDatetime() != null && o.getDatetime().toLocalDate().isAfter(to))
                return false;
            if (metodo != null && !"Todos".equals(metodo) && !metodo.equals(o.getPaymentMethod()))
                return false;
            if (empleado != null && !"Todos".equals(empleado)) {
                String empName = employeeNameMap.getOrDefault(o.getIdEmployee(), "");
                if (!empName.equals(empleado)) return false;
            }
            if (!search.isEmpty()) {
                String id = String.valueOf(o.getId());
                if (!id.contains(search)) return false;
            }
            return true;
        });
    }

    @FXML
    private void onClear() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        metodoPicker.setValue("Todos");
        empleadoPicker.setValue("Todos");
        searchField.clear();
        filtered.setPredicate(p -> true);
    }

    @FXML
    private void onRowClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            OrderResponse selected = pedidosTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            openPedidoDetail(selected);
        }
    }

    private void openPedidoDetail(OrderResponse order) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/pedidoDetail.fxml"));
            Parent detailRoot = loader.load();
            PedidoDetailController ctrl = loader.getController();
            ctrl.populate(order, employeeNameMap);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(pedidosTable.getScene().getWindow());
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
