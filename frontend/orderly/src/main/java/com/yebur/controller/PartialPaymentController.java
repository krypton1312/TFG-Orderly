package com.yebur.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PartialPaymentController {

    @FXML
    private TableView<?> mainOrderTable;

    @FXML
    private TableView<?> partialOrderTable;

    @FXML
    private Label mainTotalLabel;

    @FXML
    private Label partialTotalLabel;

    @FXML
    private Label tableNameLabel;

    @FXML
    private TextField displayField;

    @FXML
    public void initialize() {
        displayField.setText("0");
    }

    @FXML
    private void handleCloseClick() {
        Stage stage = (Stage) tableNameLabel.getScene().getWindow();
        stage.close();
    }
}
