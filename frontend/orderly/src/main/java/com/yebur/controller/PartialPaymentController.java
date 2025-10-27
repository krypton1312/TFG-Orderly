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
        String input = displayField.getText().trim();
        int amountToMove;

        if (input.isEmpty()) {
            amountToMove = 1;
        } else {
            try {
                amountToMove = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Неверное количество: " + input);
                displayField.clear();
                return;
            }
        }

        if (amountToMove <= 0) {
            System.out.println("⚠ Количество должно быть больше нуля");
            displayField.clear();
            return;
        }

        // Если пользователь хочет больше, чем есть — переносим всё
        if (amountToMove >= item.getAmount()) {
            // Проверяем, есть ли уже копия в partialDetails
            OrderDetailResponse existing = partialDetails.stream()
                    .filter(p -> p.getProductId().equals(item.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setAmount(existing.getAmount() + item.getAmount());
            } else {
                partialDetails.add(item);
            }

            orderDetails.remove(item);
        } else {
            // Уменьшаем количество в основном заказе
            item.setAmount(item.getAmount() - amountToMove);

            // Проверяем, есть ли уже такой продукт в частичном заказе
            OrderDetailResponse existing = partialDetails.stream()
                    .filter(p -> p.getProductId().equals(item.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setAmount(existing.getAmount() + amountToMove);
            } else {
                // Создаём временный элемент для UI
                OrderDetailResponse uiCopy = new OrderDetailResponse();
                uiCopy.setProductId(item.getProductId());
                uiCopy.setProductName(item.getProductName());
                uiCopy.setAmount(amountToMove);
                uiCopy.setUnitPrice(item.getUnitPrice());
                uiCopy.setStatus(item.getStatus());
                uiCopy.setComment(item.getComment());
                uiCopy.setOrderId(item.getOrderId());
                partialDetails.add(uiCopy);
            }

            // Создаём новый запрос для БД
            OrderDetailRequest newRequest = new OrderDetailRequest(
                    item.getProductId(),
                    item.getOrderId(),
                    item.getComment(),
                    amountToMove,
                    item.getUnitPrice(),
                    item.getStatus());
            partialDetailsNew.add(newRequest);
        }

        System.out.println(partialDetails);
        displayField.clear();
        refreshUI();
    }

    private void moveItemToMain(OrderDetailResponse item) {
        String input = displayField.getText().trim();
        int amountToMove;

        if (input.isEmpty()) {
            amountToMove = 1;
        } else {
            try {
                amountToMove = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                displayField.clear();
                return;
            }
        }

        if (amountToMove <= 0) {
            displayField.clear();
            return;
        }

        OrderDetailResponse existing = orderDetails.stream()
                .filter(p -> p.getProductId().equals(item.getProductId()))
                .findFirst()
                .orElse(null);

        if (amountToMove >= item.getAmount()) {
            if (existing != null) {
                existing.setAmount(existing.getAmount() + item.getAmount());
            } else {
                orderDetails.add(item);
            }
            partialDetails.remove(item);
        } else {
            item.setAmount(item.getAmount() - amountToMove);

            if (existing != null) {
                existing.setAmount(existing.getAmount() + amountToMove);
            } else {
                OrderDetailResponse newItem = new OrderDetailResponse();
                newItem.setProductId(item.getProductId());
                newItem.setProductName(item.getProductName());
                newItem.setAmount(amountToMove);
                newItem.setUnitPrice(item.getUnitPrice());
                newItem.setStatus(item.getStatus());
                newItem.setComment(item.getComment());
                newItem.setOrderId(item.getOrderId());
                orderDetails.add(newItem);
            }
            System.out.println(orderDetails);
        }

        displayField.clear();
        refreshUI();
    }

    @FXML
    private void handlePayNoReceipt() {
        persistPartialDetails();
        List<Long> ids = partialDetails.stream()
                .map(OrderDetailResponse::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return;
        }
        try {
            OrderDetailService.changeOrderDetailStatus(ids, "PAID");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(partialDetails);
        System.out.println(partialDetailsNew);

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
            case "C":
                displayField.clear();
                break;
            case "+-":
                break;
            case "/":
            case "*":
            case "-":
            case "+":
                // setOperator(buttonText);
                break;
            case "=":
                // calculateResult();
                break;
            case ".":
                // addDot();
                break;
            default:
                addDigit(buttonText);
                break;
        }
    }

    private void addDigit(String digit) {
        displayField.setText(displayField.getText() + digit);
    }

    private void persistPartialDetails() {
        try {
            partialDetails.removeIf(detail -> detail.getId() == null);

            List<Long> idsToUpdate = new ArrayList<>();
            List<OrderDetailRequest> requestsToUpdate = new ArrayList<>();

            for (OrderDetailResponse od : orderDetails) {
                if (od.getId() != null) {
                    idsToUpdate.add(od.getId());
                    requestsToUpdate.add(new OrderDetailRequest(
                            od.getProductId(),
                            od.getOrderId(),
                            od.getComment(),
                            od.getAmount(),
                            od.getUnitPrice(),
                            od.getStatus()));
                }
            }

            if (!idsToUpdate.isEmpty()) {
                OrderDetailService.updateOrderDetailList(idsToUpdate, requestsToUpdate);
            }

            if (!partialDetailsNew.isEmpty()) {
                List<OrderDetailResponse> created = OrderDetailService.createOrderDetailList(partialDetailsNew);
                System.out.println(created);
                partialDetails.addAll(created);
                partialDetailsNew.clear();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
