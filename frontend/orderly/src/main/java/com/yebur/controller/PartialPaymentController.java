package com.yebur.controller;

import java.util.List;

import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.ProductResponse;
import com.yebur.model.response.RestTableResponse;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PartialPaymentController {

    @FXML
    private VBox mainOrderBox;

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
        Platform.runLater(() -> displayField.getParent().requestFocus());

        displayField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                displayField.deselect();
            }
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
        this.orderDetails = primaryController.getCurrentdetails();
        this.table = primaryController.getSelectedTable();
        this.mainTotal = order.getTotal();
        partialTotal = 0.0;
        tableNameLabel.setText(table.getName());
        renderDetails(orderDetails);
        partialTotalLabel.setText(String.format("%.2f", partialTotal));
    }

    private void renderDetails(List<OrderDetailResponse> details) {
        mainOrderBox.getChildren().clear();
        double total = 0;

        for (OrderDetailResponse d : details) {
            HBox row = new HBox(10);
            row.getStyleClass().add("order-item-row");

            Label qty = new Label("x" + d.getAmount());
            qty.setPrefWidth(60);
            qty.getStyleClass().add("order-item-row");

            String name = d.getProductName();
            Label nameLabel = new Label(name);
            nameLabel.setPrefWidth(235);
            nameLabel.setWrapText(true);

            Label priceLabel = new Label(String.format("$%.2f", d.getUnitPrice()));
            priceLabel.setPrefWidth(100);
            priceLabel.getStyleClass().add("order-item-row");

            double totalLine = d.getUnitPrice() * d.getAmount();
            Label totalLabel = new Label(String.format("$%.2f", totalLine));
            totalLabel.setPrefWidth(100);
            totalLabel.getStyleClass().add("order-item-row");

            row.getChildren().addAll(qty, nameLabel, priceLabel, totalLabel);

            mainOrderBox.getChildren().add(row);
            total += totalLine;
        }

        mainTotalLabel.setText(String.format("$%.2f", total));

    }
}
