package com.yebur.controller;

import com.yebur.model.response.EmployeeResponse;
import com.yebur.service.EmployeeService;
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

public class EmpleadosListController {

    @FXML private BorderPane root;
    @FXML private TextField searchField;
    @FXML private TableView<EmployeeResponse> empleadosTable;
    @FXML private TableColumn<EmployeeResponse, String> idCol;
    @FXML private TableColumn<EmployeeResponse, String> nameCol;
    @FXML private TableColumn<EmployeeResponse, String> emailCol;
    @FXML private TableColumn<EmployeeResponse, String> rolCol;
    @FXML private TableColumn<EmployeeResponse, String> statusCol;

    private final ObservableList<EmployeeResponse> allItems = FXCollections.observableArrayList();
    private FilteredList<EmployeeResponse> filtered;

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
        nameCol.setCellValueFactory(c -> {
            EmployeeResponse e = c.getValue();
            String full = (e.getName() != null ? e.getName() : "") + " " + (e.getLastname() != null ? e.getLastname() : "");
            return new javafx.beans.property.SimpleStringProperty(full.trim());
        });
        emailCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEmail() != null ? c.getValue().getEmail() : "—"));
        rolCol.setCellValueFactory(c -> {
            var roles = c.getValue().getRoles();
            String rol = (roles != null && !roles.isEmpty()) ? roles.iterator().next().toString() : "—";
            return new javafx.beans.property.SimpleStringProperty(rol);
        });
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatus() != null ? c.getValue().getStatus() : "—"));

        filtered = new FilteredList<>(allItems, p -> true);
        SortedList<EmployeeResponse> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(empleadosTable.comparatorProperty());
        empleadosTable.setItems(sorted);

        searchField.textProperty().addListener((obs, o, val) -> {
            String q = val != null ? val.trim().toLowerCase() : "";
            filtered.setPredicate(e -> {
                if (q.isEmpty()) return true;
                String full = ((e.getName() != null ? e.getName() : "") + " " +
                               (e.getLastname() != null ? e.getLastname() : "")).toLowerCase();
                return full.contains(q)
                        || (e.getEmail() != null && e.getEmail().toLowerCase().contains(q));
            });
        });

        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<EmployeeResponse> emps = EmployeeService.getAllEmployees();
                Platform.runLater(() -> allItems.setAll(emps));
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
            EmployeeResponse selected = empleadosTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            openEmpleadoDetail(selected);
        }
    }

    private void openEmpleadoDetail(EmployeeResponse emp) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/empleadoDetail.fxml"));
            Parent detailRoot = loader.load();
            EmpleadoDetailController ctrl = loader.getController();
            ctrl.populate(emp);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(empleadosTable.getScene().getWindow());
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
