package com.yebur.controller;

import com.yebur.model.response.CashOperationResponse;
import com.yebur.model.response.CashSessionResponse;

import com.yebur.service.CashOperationService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


public class OperationsController {
    @FXML private Label totalEntradas;
    @FXML private Label totalSalidas;
    @FXML private Label totalCaja;
    @FXML private TableView<CashOperationResponse> table;

    @FXML private TableColumn<CashOperationResponse, Number> colT;
    @FXML private TableColumn<CashOperationResponse, String> colTipo;
    @FXML private TableColumn<CashOperationResponse, String> colConcepto;
    @FXML private TableColumn<CashOperationResponse, BigDecimal> colEntrada;
    @FXML private TableColumn<CashOperationResponse, BigDecimal> colSalida;

    private CashSessionResponse actualSession;
    private List<CashOperationResponse> operations;
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal outcome = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    @FXML
    public void initialize() {
        actualSession = StartController.getCashSession();
        try{
            setOperations(CashOperationService.getCashOperationsBySessionId(actualSession.getId()));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        // ===== value factories =====
        colT.setCellValueFactory(c ->
                new SimpleIntegerProperty(
                        table.getItems().indexOf(c.getValue()) + 1
                )
        );

        colTipo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getType())
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
        colTipo.getStyleClass().add("cell-center");
        colConcepto.getStyleClass().add("cell-center");
        colEntrada.getStyleClass().add("cell-center");
        colSalida.getStyleClass().add("cell-center");

        // ===== cell factories =====
        setupTipoColumn();
        setupAmountColumn(colEntrada, true);
        setupAmountColumn(colSalida, false);

    }
    private void setupAmountColumn(
            TableColumn<CashOperationResponse, BigDecimal> column,
            boolean isEntrada
    ) {
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

                Label pill = new Label(
                        value.setScale(2, BigDecimal.ROUND_HALF_UP) + " €"
                );

                pill.getStyleClass().addAll(
                        "amount-pill",
                        isEntrada ? "amount-green" : "amount-red"
                );

                setText(null);
                setGraphic(pill);
            }
        });
    }



    private void setupTipoColumn() {
        colTipo.setCellFactory(col -> new TableCell<>() {
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

    public void setTotals(List<CashOperationResponse> operations){
        for(CashOperationResponse co: operations){
            if(co.getType().equals("ENTRADA")){
                income = income.add(co.getAmount());
            }else{
                outcome = outcome.add(co.getAmount());
            }
        }

        total = income.subtract(outcome);
        totalEntradas.setText(currencyFormatter.format(income));
        totalSalidas.setText(currencyFormatter.format(outcome));
        totalCaja.setText(currencyFormatter.format(total));
    }

    public void onAnular(ActionEvent actionEvent) {
    }

    public void onCajon(ActionEvent actionEvent) {
    }

    public void onSalir(ActionEvent actionEvent) {
    }

    public void onEntrada(ActionEvent actionEvent) {
    }

    public void onSalida(ActionEvent actionEvent) {

    }
}
