package com.yebur.controller;

import java.util.List;

import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.RestTableResponse;

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

    private PrimaryController primaryController;

    private List<OrderDetailResponse> orderDetails;
    private OrderResponse order;
    private RestTableResponse table;
    private double mainTotal;
    private double partialTotal;


    @FXML
    public void initialize() {
        displayField.setText(" ");
    
    }

    @FXML
    private void handleCloseClick() {
        Stage stage = (Stage) tableNameLabel.getScene().getWindow();
        stage.close();
    }

    public void setPrimaryController(PrimaryController primaryController) {
        this.primaryController = primaryController;
        loadData();
    }

    public void loadData(){
        this.order = primaryController.getCurrentOrder();
        this.orderDetails = primaryController.getCurrentdetails();
        this.table = primaryController.getSelectedTable();
        this.mainTotal = order.getTotal();
        partialTotal = 0.0;
        tableNameLabel.setText(table.getName());
        mainTotalLabel.setText(String.format("%.2f", mainTotal));
        partialTotalLabel.setText(String.format("%.2f", partialTotal));
    }
}
