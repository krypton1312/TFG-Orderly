package com.yebur.controller;

import java.util.ArrayList;
import java.util.List;

import com.yebur.model.request.OrderDetailRequest;
import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.RestTableResponse;
import com.yebur.service.OrderDetailService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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

    @FXML
    private GridPane calculatorGP;

    private PrimaryController primaryController;

    private List<OrderDetailResponse> orderDetails;
    private List<OrderDetailResponse> partialDetails;
    private List<OrderDetailRequest> partialDetailsNew;

    private OrderResponse order;
    private RestTableResponse table;

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
        int amountToMove = parseInputAmount();
        if (amountToMove <= 0)
            return;

        if (amountToMove >= item.getAmount()) {
            // Полный перенос — создаём копию
            OrderDetailResponse copy = copyResponse(item);
            copy.setStatus("PAID");
            partialDetails.add(copy);
            orderDetails.remove(item);
        } else {
            // Частичный перенос
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
                            od.getStatus()
                    ));
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
            System.out.println("⚠ Ошибка при сохранении частичных оплат: " + e.getMessage());
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
}
