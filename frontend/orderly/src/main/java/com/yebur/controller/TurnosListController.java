package com.yebur.controller;

import com.yebur.model.response.CashOperationResponse;
import com.yebur.model.response.CashSessionResponse;
import com.yebur.service.CashOperationService;
import com.yebur.service.CashSessionService;
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
import java.util.List;

public class TurnosListController {

    @FXML private BorderPane root;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<CashSessionResponse> turnosTable;
    @FXML private TableColumn<CashSessionResponse, String> idCol;
    @FXML private TableColumn<CashSessionResponse, String> dateCol;
    @FXML private TableColumn<CashSessionResponse, String> shiftNoCol;
    @FXML private TableColumn<CashSessionResponse, String> openedAtCol;
    @FXML private TableColumn<CashSessionResponse, String> closedAtCol;
    @FXML private TableColumn<CashSessionResponse, String> totalCashCol;
    @FXML private TableColumn<CashSessionResponse, String> totalCardCol;
    @FXML private TableColumn<CashSessionResponse, String> statusCol;

    private final ObservableList<CashSessionResponse> allItems = FXCollections.observableArrayList();
    private FilteredList<CashSessionResponse> filtered;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
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

        com.yebur.ui.DatePickerStyler.apply(fromDatePicker);
        com.yebur.ui.DatePickerStyler.apply(toDatePicker);

        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getId())));
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getBusinessDate() != null ? c.getValue().getBusinessDate().format(dateFmt) : "—"));
        shiftNoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getShiftNo())));
        openedAtCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getOpenedAt() != null ? c.getValue().getOpenedAt().format(timeFmt) : "—"));
        closedAtCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getClosedAt() != null ? c.getValue().getClosedAt().format(timeFmt) : "—"));
        totalCashCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getTotalSalesCash() != null ? moneyFmt.format(c.getValue().getTotalSalesCash()) + " €" : "—"));
        totalCardCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getTotalSalesCard() != null ? moneyFmt.format(c.getValue().getTotalSalesCard()) + " €" : "—"));
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                "CLOSED".equals(c.getValue().getStatus()) ? "Cerrado" : "Abierto"));

        filtered = new FilteredList<>(allItems, p -> true);
        SortedList<CashSessionResponse> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(turnosTable.comparatorProperty());
        turnosTable.setItems(sorted);

        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<CashSessionResponse> sessions = CashSessionService.getAllCashSessions();
                Platform.runLater(() -> allItems.setAll(sessions));
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
        filtered.setPredicate(s -> {
            if (s.getBusinessDate() == null) return false;
            if (from != null && s.getBusinessDate().isBefore(from)) return false;
            if (to != null && s.getBusinessDate().isAfter(to)) return false;
            return true;
        });
    }

    @FXML
    private void onClear() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        filtered.setPredicate(p -> true);
    }

    @FXML
    private void onRowClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            CashSessionResponse selected = turnosTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            openShiftCloseReport(selected);
        }
    }

    private void openShiftCloseReport(CashSessionResponse session) {
        try {
            List<CashOperationResponse> ops = CashOperationService.getCashOperationsBySessionId(session.getId());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/shiftCloseReport.fxml"));
            Parent reportRoot = loader.load();
            ShiftCloseReportController ctrl = loader.getController();
            ctrl.populate(session, ops);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(turnosTable.getScene().getWindow());
            Scene scene = new Scene(reportRoot);
            com.yebur.ui.ThemeSupport.copyTheme(reportRoot, scene, turnosTable.getScene());
            stage.setScene(scene);
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
