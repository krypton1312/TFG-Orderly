package com.yebur.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

import com.yebur.model.request.OrderDetailRequest;
import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.RestTableResponse;
import com.yebur.service.OrderDetailService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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

    @FXML
    private GridPane calculatorGP;

    private PrimaryController primaryController;

    private List<OrderDetailResponse> orderDetails;
    private List<OrderDetailResponse> partialDetails;
    private List<OrderDetailRequest> partialDetailsNew;

    private OrderResponse order;
    private RestTableResponse table;
    private double total_check;

    private boolean isPaymentBoxShown = false;

    @FXML
    public void initialize() {
        Platform.runLater(() -> displayField.getParent().requestFocus());
        for (Node node : calculatorGP.getChildren()) {
            if (node instanceof Button button) {
                button.setOnAction(this::handleButtonClick);
            }
        }
        displayField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal)
                displayField.deselect();
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
        this.partialDetailsNew = new ArrayList<>();
        this.table = primaryController.getSelectedTable();
        System.out.println(table);
        tableNameLabel.setText((this.table != null ? table.getName() + " - " : "") + "Cuenta #" + order.getId());
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
                    if (isPaymentBoxShown) {
                        partialDetails.clear();
                    }
                    moveItemToPartial(d);
                } else {
                    moveItemToMain(d);
                }
            });

            box.getChildren().add(row);
            total += totalLine;
            if (total > 0) {
                total_check = total;
            }
        }

        if (isMainBox) {
            mainTotalLabel.setText(String.format("$%.2f", total));
        } else {
            partialTotalLabel.setText(String.format("$%.2f", total));
        }
    }

    private void moveItemToPartial(OrderDetailResponse item) {
        int amountToMove = parseInputAmount();
        if (amountToMove <= 0)
            return;

        if (amountToMove >= item.getAmount()) {
            OrderDetailResponse copy = copyResponse(item);
            copy.setStatus("PAID");
            partialDetails.add(copy);
            orderDetails.remove(item);
        } else {
            item.setAmount(item.getAmount() - amountToMove);

            OrderDetailResponse existing = partialDetails.stream()
                    .filter(p -> p.getProductId().equals(item.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setAmount(existing.getAmount() + amountToMove);
            } else {
                OrderDetailResponse uiCopy = new OrderDetailResponse();
                uiCopy.setProductId(item.getProductId());
                uiCopy.setProductName(item.getProductName());
                uiCopy.setAmount(amountToMove);
                uiCopy.setUnitPrice(item.getUnitPrice());
                uiCopy.setStatus("PAID");
                uiCopy.setComment(item.getComment());
                uiCopy.setOrderId(item.getOrderId());
                partialDetails.add(uiCopy);
            }

            OrderDetailRequest newRequest = new OrderDetailRequest(
                    item.getProductId(),
                    item.getOrderId(),
                    item.getComment(),
                    amountToMove,
                    item.getUnitPrice(),
                    "PAID");
            partialDetailsNew.add(newRequest);
        }

        displayField.clear();
        refreshUI();
    }

    private void moveItemToMain(OrderDetailResponse item) {
        int amountToMove = parseInputAmount();
        if (amountToMove <= 0)
            return;

        OrderDetailResponse existing = orderDetails.stream()
                .filter(p -> p.getProductId().equals(item.getProductId()))
                .findFirst()
                .orElse(null);

        if (amountToMove >= item.getAmount()) {
            if (existing != null) {
                existing.setAmount(existing.getAmount() + item.getAmount());
            } else {
                orderDetails.add(copyResponse(item));
            }
            partialDetails.remove(item);
        } else {
            item.setAmount(item.getAmount() - amountToMove);

            if (existing != null) {
                existing.setAmount(existing.getAmount() + amountToMove);
            } else {
                OrderDetailResponse newItem = copyResponse(item);
                newItem.setAmount(amountToMove);
                orderDetails.add(newItem);
            }
        }

        displayField.clear();
        refreshUI();
    }

    @FXML
    private void handlePayNoReceipt() {
        persistPartialDetails();
        partialDetails.clear();
        refreshUI();
        showPaymentBox(getTotalCheck());
        displayField.clear();

        if (orderDetails.isEmpty()) {
            Stage stage = (Stage) tableNameLabel.getScene().getWindow();
            stage.close();
        }
    }

    private void handleButtonClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();

        switch (buttonText) {
            case "C" -> displayField.clear();
            case "+-" -> {
            }
            default -> addDigit(buttonText);
        }
    }

    private void addDigit(String digit) {
        displayField.setText(displayField.getText() + digit);
    }

    private void persistPartialDetails() {
        try {
            List<Long> idsToUpdate = new ArrayList<>();
            List<OrderDetailRequest> reqsToUpdate = new ArrayList<>();

            for (OrderDetailResponse od : orderDetails) {
                if (od.getId() != null) {
                    idsToUpdate.add(od.getId());
                    reqsToUpdate.add(new OrderDetailRequest(
                            od.getProductId(),
                            od.getOrderId(),
                            od.getComment(),
                            od.getAmount(),
                            od.getUnitPrice(),
                            od.getStatus()));
                }
            }
            for (OrderDetailResponse pd : partialDetails) {
                if (pd.getId() != null) {
                    idsToUpdate.add(pd.getId());
                    reqsToUpdate.add(new OrderDetailRequest(
                            pd.getProductId(),
                            pd.getOrderId(),
                            pd.getComment(),
                            pd.getAmount(),
                            pd.getUnitPrice(),
                            "PAID"));
                }
            }

            if (!idsToUpdate.isEmpty()) {
                OrderDetailService.updateOrderDetailList(idsToUpdate, reqsToUpdate);
            }
            if (!partialDetailsNew.isEmpty()) {
                List<OrderDetailRequest> aggregated = new ArrayList<>();

                for (OrderDetailRequest req : partialDetailsNew) {
                    OrderDetailRequest existing = aggregated.stream()
                            .filter(r -> r.getProductId().equals(req.getProductId()))
                            .findFirst()
                            .orElse(null);

                    if (existing != null) {
                        existing.setAmount(existing.getAmount() + req.getAmount());
                    } else {
                        aggregated.add(new OrderDetailRequest(
                                req.getProductId(),
                                req.getOrderId(),
                                req.getComment(),
                                req.getAmount(),
                                req.getUnitPrice(),
                                "PAID"));
                    }
                }
                List<OrderDetailResponse> created = OrderDetailService.createOrderDetailList(aggregated);

                partialDetails.removeIf(d -> d.getId() == null);
                partialDetails.addAll(created);

                partialDetailsNew.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int parseInputAmount() {
        String input = displayField.getText().trim();
        if (input.isEmpty())
            return 1;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            displayField.clear();
            return -1;
        }
    }

    private OrderDetailResponse copyResponse(OrderDetailResponse src) {
        OrderDetailResponse copy = new OrderDetailResponse();
        copy.setId(src.getId());
        copy.setProductId(src.getProductId());
        copy.setProductName(src.getProductName());
        copy.setAmount(src.getAmount());
        copy.setUnitPrice(src.getUnitPrice());
        copy.setStatus(src.getStatus());
        copy.setComment(src.getComment());
        copy.setOrderId(src.getOrderId());
        return copy;
    }

    public double[] getTotalCheck() {
        double input = Double.parseDouble(!displayField.getText().isEmpty() ? displayField.getText() : "0");
        double change = input - total_check;
        return new double[] { total_check, input, change };
    }

    private void showPaymentBox(double[] paymentInfo) {
        
        partialOrderBox.getChildren().clear();

        double total = paymentInfo[0];
        double recibido = paymentInfo[1];
        double cambio = paymentInfo[2];

        StackPane wrapper = new StackPane();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefSize(partialOrderBox.getWidth(), partialOrderBox.getHeight()*0.5);

        VBox paymentVB = new VBox();
        paymentVB.setAlignment(Pos.CENTER_LEFT);
        paymentVB.setPadding(new javafx.geometry.Insets(15, 15, 15, 15));
        paymentVB.setSpacing(10);
        paymentVB.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 4);" +
                        "-fx-border-color: #f3f4f6;" +
                        "-fx-border-radius: 12;");

        paymentVB.setMaxWidth(partialOrderBox.getWidth() * 0.9);
        paymentVB.setMaxHeight(partialOrderBox.getHeight() * 0.9);
        paymentVB.setPrefHeight(partialOrderBox.getHeight() * 0.9);
        paymentVB.setPrefWidth(partialOrderBox.getWidth() * 0.9);

        StackPane.setAlignment(paymentVB, Pos.CENTER);
        StackPane.setMargin(paymentVB, new javafx.geometry.Insets(40, 0, 0, 0)); 

        Label title = new Label("💳 PAGO DE LA SUBCUENTA");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Pane separator = new Pane();
        separator.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        separator.setPrefHeight(1);

        HBox rowTotal = createPaymentRow("COBRADO:", String.format("%.2f €", total), "#000");
        HBox rowRecibido = createPaymentRow("RECIBIDO:", String.format("%.2f €", recibido), "#000");
        HBox rowCambio = createPaymentRow("CAMBIO:", String.format("%.2f €", cambio), "#16a34a");

        if(recibido > 0){
            paymentVB.getChildren().addAll(title, separator, rowTotal, rowRecibido, rowCambio);
        }else{
            paymentVB.getChildren().addAll(title, separator, rowTotal);
        }

        wrapper.getChildren().add(paymentVB);

        partialOrderBox.getChildren().add(wrapper);

        paymentVB.setOpacity(0);
        paymentVB.setScaleX(0.9);
        paymentVB.setScaleY(0.9);
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300),
                paymentVB);
        fade.setFromValue(0);
        fade.setToValue(1);
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300),
                paymentVB);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1);
        scale.setToY(1);
        new javafx.animation.ParallelTransition(fade, scale).play();

        isPaymentBoxShown = true;
    }

    private HBox createPaymentRow(String label, String value, String color) {
        HBox row = new HBox(7);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblText = new Label(label);
        lblText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #000;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 18px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

        HBox.setHgrow(lblText, javafx.scene.layout.Priority.ALWAYS);
        row.getChildren().addAll(lblText, lblValue);
        return row;
    }

}
