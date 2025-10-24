package com.yebur.controller;

import java.util.ArrayList;
import java.util.List;

import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.RestTableResponse;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PartialPaymentController {

    @FXML
    private VBox mainOrderBox;

    @FXML
    private VBox partialOrderBox;

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
    private List<OrderDetailResponse> partialDetails;

    private OrderResponse order;
    private RestTableResponse table;

    @FXML
    public void initialize() {
        Platform.runLater(() -> displayField.getParent().requestFocus());
        displayField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) displayField.deselect();
        });
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

    public void loadData() {
        this.order = primaryController.getCurrentOrder();
        this.orderDetails = new ArrayList<>(primaryController.getCurrentdetails());
        this.partialDetails = new ArrayList<>();
        this.table = primaryController.getSelectedTable();

        tableNameLabel.setText(table.getName());
        refreshUI();
    }

    private void refreshUI() {
        renderDetails(orderDetails, mainOrderBox, true);
        renderDetails(partialDetails, partialOrderBox, false);
    }

    private void renderDetails(List<OrderDetailResponse> details, VBox box, boolean isMainBox) {
        box.getChildren().clear();
        double total = 0.0;

        for (OrderDetailResponse d : details) {
            HBox row = new HBox(10);
            row.getStyleClass().add("order-item-row");

            Label qtyLabel = new Label("x" + d.getAmount());
            qtyLabel.setPrefWidth(60);

            Label nameLabel = new Label(d.getProductName());
            nameLabel.setPrefWidth(235);
            nameLabel.setWrapText(true);

            Label priceLabel = new Label(String.format("$%.2f", d.getUnitPrice()));
            priceLabel.setPrefWidth(100);

            double totalLine = d.getUnitPrice() * d.getAmount();
            Label totalLabel = new Label(String.format("$%.2f", totalLine));
            totalLabel.setPrefWidth(100);

            row.getChildren().addAll(qtyLabel, nameLabel, priceLabel, totalLabel);

            row.setOnMouseClicked(event -> {
                if (isMainBox) {
                    moveItemToPartial(d);
                } else {
                    moveItemToMain(d);
                }
            });

            box.getChildren().add(row);
            total += totalLine;
        }

        if (isMainBox) {
            mainTotalLabel.setText(String.format("$%.2f", total));
        } else {
            partialTotalLabel.setText(String.format("$%.2f", total));
        }
    }
    private void moveItemToPartial(OrderDetailResponse item) {
        orderDetails.remove(item);
        partialDetails.add(item);
        refreshUI();
    }
    private void moveItemToMain(OrderDetailResponse item) {
        partialDetails.remove(item);
        orderDetails.add(item);
        refreshUI();
    }

    @FXML
    private void handlePayNoReceipt(){
        
    }
}
