package com.yebur.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yebur.model.response.EmployeeResponse;
import com.yebur.service.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class EmpleadoDetailController {

    @FXML private BorderPane root;
    @FXML private Label titleLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label statusLabel;
    @FXML private Label rolLabel;
    @FXML private Label noShiftsLabel;
    @FXML private TableView<JsonNode> shiftsTable;
    @FXML private TableColumn<JsonNode, String> shiftIdCol;
    @FXML private TableColumn<JsonNode, String> shiftDateCol;
    @FXML private TableColumn<JsonNode, String> shiftInCol;
    @FXML private TableColumn<JsonNode, String> shiftOutCol;

    private final ObservableList<JsonNode> shiftItems = FXCollections.observableArrayList();
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        root.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null) newScene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE)
                    ((Stage) newScene.getWindow()).close();
            });
        });

        shiftIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().path("id").asText("—")));
        shiftDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().path("date").asText(c.getValue().path("businessDate").asText("—"))));
        shiftInCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().path("checkIn").asText(c.getValue().path("clockIn").asText("—"))));
        shiftOutCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().path("checkOut").asText(c.getValue().path("clockOut").asText("—"))));

        shiftsTable.setItems(shiftItems);
    }

    public void populate(EmployeeResponse emp) {
        titleLabel.setText("DETALLE: " +
                ((emp.getName() != null ? emp.getName() : "") + " " +
                 (emp.getLastname() != null ? emp.getLastname() : "")).trim().toUpperCase());
        nameLabel.setText((emp.getName() != null ? emp.getName() : "") + " " +
                          (emp.getLastname() != null ? emp.getLastname() : ""));
        emailLabel.setText(emp.getEmail() != null ? emp.getEmail() : "—");
        statusLabel.setText(emp.getStatus() != null ? emp.getStatus() : "—");
        var roles = emp.getRoles();
        rolLabel.setText((roles != null && !roles.isEmpty()) ? roles.iterator().next().toString() : "—");

        Thread t = new Thread(() -> {
            try {
                String json = ApiClient.get("/shift-records/employee/" + emp.getId());
                List<JsonNode> shifts = mapper.readValue(json, new TypeReference<List<JsonNode>>() {});
                Platform.runLater(() -> {
                    shiftItems.setAll(shifts);
                    boolean none = shifts.isEmpty();
                    noShiftsLabel.setVisible(none);
                    noShiftsLabel.setManaged(none);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    shiftItems.clear();
                    noShiftsLabel.setVisible(true);
                    noShiftsLabel.setManaged(true);
                    shiftsTable.setVisible(false);
                    shiftsTable.setManaged(false);
                });
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
