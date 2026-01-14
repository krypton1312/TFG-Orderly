package com.yebur.controller;

import com.yebur.app.App;
import com.yebur.model.response.CashOperationResponse;
import com.yebur.model.response.CashSessionResponse;
import com.yebur.service.CashOperationService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OperationsController {

    @FXML private Label totalEntradas;
    @FXML private Label totalSalidas;
    @FXML private Label totalCaja;

    @FXML private TableView<CashOperationResponse> table;

    @FXML private TableColumn<CashOperationResponse, Number> colT;
    @FXML private TableColumn<CashOperationResponse, String> colPaymentMethod;
    @FXML private TableColumn<CashOperationResponse, String> colConcepto;
    @FXML private TableColumn<CashOperationResponse, BigDecimal> colEntrada;
    @FXML private TableColumn<CashOperationResponse, BigDecimal> colSalida;

    @FXML private Button outflowButton;
    @FXML private Button inflowButton;

    private CashSessionResponse actualSession;
    private List<CashOperationResponse> operations;

    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal outcome = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    @FXML
    public void initialize() {
        actualSession = StartController.getCashSession();
        reloadOperations();

        // ===== value factories =====
        colT.setCellValueFactory(c ->
                new SimpleIntegerProperty(table.getItems().indexOf(c.getValue()) + 1)
        );

        colPaymentMethod.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPaymentMethod())
        );

        colConcepto.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDescription())
        );

        // ENTRADA / SALIDA — зависят от type
        colEntrada.setCellValueFactory(c -> {
            if ("ENTRADA".equalsIgnoreCase(c.getValue().getType())) {
                return new SimpleObjectProperty<>(c.getValue().getAmount());
            }
            return new SimpleObjectProperty<>(BigDecimal.ZERO);
        });

        colSalida.setCellValueFactory(c -> {
            if ("SALIDA".equalsIgnoreCase(c.getValue().getType())
                    || "PAGOS".equalsIgnoreCase(c.getValue().getType())) {
                return new SimpleObjectProperty<>(c.getValue().getAmount());
            }
            return new SimpleObjectProperty<>(BigDecimal.ZERO);
        });

        // ===== alignment classes (из CSS) =====
        colT.getStyleClass().add("cell-center");
        colPaymentMethod.getStyleClass().add("cell-center");
        colConcepto.getStyleClass().add("cell-center");
        colEntrada.getStyleClass().add("cell-center");
        colSalida.getStyleClass().add("cell-center");

        // ===== cell factories =====
        setupTipoColumn();
        setupAmountColumn(colEntrada, true);
        setupAmountColumn(colSalida, false);
    }

    // ==========================
    // Table rendering / Totals
    // ==========================

    private void printOperations() {
        table.getItems().clear();
        if (operations != null && !operations.isEmpty()) {
            table.getItems().addAll(operations);
        }
    }

    public void setOperations(List<CashOperationResponse> operations) {
        this.operations = operations;
        printOperations();
        setTotals(operations);
    }

    public void setTotals(List<CashOperationResponse> operations) {
        // IMPORTANT: reset, иначе суммы будут накапливаться
        income = BigDecimal.ZERO;
        outcome = BigDecimal.ZERO;

        if (operations != null) {
            for (CashOperationResponse co : operations) {
                if ("ENTRADA".equalsIgnoreCase(co.getType())) {
                    income = income.add(co.getAmount());
                } else {
                    outcome = outcome.add(co.getAmount());
                }
            }
        }

        total = income.subtract(outcome);
        totalEntradas.setText(currencyFormatter.format(income));
        totalSalidas.setText(currencyFormatter.format(outcome));
        totalCaja.setText(currencyFormatter.format(total));
    }

    private void reloadOperations() {
        try {
            if (actualSession == null) return;
            setOperations(CashOperationService.getCashOperationsBySessionId(actualSession.getId()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // ==========================
    // Columns setup
    // ==========================

    private void setupAmountColumn(TableColumn<CashOperationResponse, BigDecimal> column, boolean isEntrada) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
                    setText("");
                    setGraphic(null);
                    getStyleClass().add("dash");
                    return;
                }

                // NOTE: BigDecimal.ROUND_HALF_UP deprecated, но оставляю как у тебя
                Label pill = new Label(value.setScale(2, BigDecimal.ROUND_HALF_UP) + " €");
                pill.getStyleClass().addAll("amount-pill", isEntrada ? "amount-green" : "amount-red");

                setText(null);
                setGraphic(pill);
            }
        });
    }

    private void setupTipoColumn() {
        colPaymentMethod.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                getStyleClass().removeAll("tipo-efectivo", "tipo-pagos");

                if (empty || tipo == null) {
                    setText(null);
                    return;
                }

                setText(tipo.toUpperCase());

                if ("PAGOS".equalsIgnoreCase(tipo)) {
                    getStyleClass().add("tipo-pagos");
                } else {
                    getStyleClass().add("tipo-efectivo");
                }
            }
        });
    }

    // ==========================
    // Actions
    // ==========================

    public void onAnular(ActionEvent actionEvent) { }
    public void onCajon(ActionEvent actionEvent)  { }

    @FXML
    public void openJournalEntryWindow(ActionEvent actionEvent) {
        try {
            URL fxml = getClass().getResource("/com/yebur/pos/journalEntry.fxml");
            if (fxml == null) {
                System.err.println("FXML not found: /com/yebur/pos/journalEntry.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof JournalEntryController journalEntryController) {
                journalEntryController.setPaymentType(selectPaymentType(actionEvent));
            }

            Stage stage = new Stage();
            stage.setTitle("Apuntar pago");
            Scene scene = new Scene(root);
            stage.initStyle(StageStyle.DECORATED);
            stage.setResizable(true);
            stage.setScene(scene);

            // ВАЖНО: обновляем всегда после закрытия окна любым способом
            stage.setOnHidden(e -> reloadOperations());

            URL cssUrl = App.class.getResource("/com/yebur/pos/journalEntry.css");
            if (cssUrl != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String selectPaymentType(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        if (source == inflowButton) {
            return "DEPOSIT";
        } else if (source == outflowButton) {
            return "WITHDRAW";
        }
        return "";
    }

    public void onClose(ActionEvent actionEvent) {
        Stage stage = (Stage) totalSalidas.getScene().getWindow();
        stage.close();
    }
}
