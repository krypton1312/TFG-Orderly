package com.yebur.controller;

import com.yebur.model.request.OrderDetailRequest;
import com.yebur.model.request.OrderRequest;
import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.RestTableResponse;
import com.yebur.service.OrderDetailService;
import com.yebur.service.OrderService;
import com.yebur.service.ReceiptFxToPdfService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.yebur.ui.CustomDialog.showError;

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

    @FXML
    private Button cardButton;

    @FXML
    private Button cashButton;

    private PosController primaryController;

    private List<OrderDetailResponse> orderDetails;
    private List<OrderDetailResponse> partialDetails;

    private String selectedPaymentMethod = "";

    private OrderResponse order;
    private RestTableResponse table;

    private boolean isPaymentBoxShown = false;
    private boolean anyPaymentDone = false;

    private BigDecimal total_check = BigDecimal.ZERO;
    private BigDecimal input = BigDecimal.ZERO;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    // чек через FXML → snapshot → PDF
    private final ReceiptFxToPdfService receiptFxToPdfService = new ReceiptFxToPdfService();

    private PaymentInfo lastPaymentInfo;

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

        cardButton.setOnAction(e -> selectPayment("card"));
        cashButton.setOnAction(e -> selectPayment("cash"));
    }

    @FXML
    private void handleCloseClick() {
        Stage stage = (Stage) tableNameLabel.getScene().getWindow();
        stage.close();
    }

    public void setPrimaryController(PosController primaryController) {
        this.primaryController = primaryController;
        loadData();
    }

    public void loadData() {
        this.order = primaryController.getCurrentOrder();
        this.orderDetails = primaryController.getCurrentdetails();
        System.out.println(orderDetails);
        this.partialDetails = new ArrayList<>();
        this.table = primaryController.getSelectedTable();
        tableNameLabel.setText(
                order != null ?
                        (this.table != null ? table.getName() + " - " : "") + "Cuenta #" + order.getId()
                        : " ");
        refreshUI();
    }

    private void refreshUI() {
        renderDetails(orderDetails, mainOrderBox, true);
        renderDetails(partialDetails, partialOrderBox, false);
    }

    private void renderDetails(List<OrderDetailResponse> details, VBox box, boolean isMainBox) {
        box.getChildren().clear();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderDetailResponse d : details) {

            // 🔹 НЕ показываем строки с amount <= 0, но они остаются в списке для обновления в БД
            if (d.getAmount() <= 0) {
                continue;
            }

            GridPane row = new GridPane();
            row.getStyleClass().add("order-item-row");
            row.setHgap(10);

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(10);
            col1.setHalignment(javafx.geometry.HPos.CENTER);

            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(50);
            col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(20);
            col3.setHalignment(javafx.geometry.HPos.CENTER);

            ColumnConstraints col4 = new ColumnConstraints();
            col4.setPercentWidth(20);
            col4.setHalignment(javafx.geometry.HPos.CENTER);

            row.getColumnConstraints().addAll(col1, col2, col3, col4);

            // 1. Кол-во
            Label qtyLabel = new Label("x" + d.getAmount());
            qtyLabel.setAlignment(Pos.CENTER);
            qtyLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHalignment(qtyLabel, javafx.geometry.HPos.CENTER);
            row.add(qtyLabel, 0, 0);

            // 2. Название
            String name = d.getName() != null ? d.getName() : "Producto #" + d.getProductId();
            Label nameLabel = new Label(name);
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);
            row.add(nameLabel, 1, 0);

            // 3. Цена за единицу
            BigDecimal unitPrice = d.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
            Label priceLabel = new Label(currencyFormatter.format(unitPrice));
            priceLabel.setAlignment(Pos.CENTER_RIGHT);
            priceLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHalignment(priceLabel, javafx.geometry.HPos.CENTER);
            row.add(priceLabel, 2, 0);

            // 4. Сумма по строке
            BigDecimal totalLine = unitPrice
                    .multiply(BigDecimal.valueOf(d.getAmount()))
                    .setScale(2, RoundingMode.HALF_UP);
            Label totalLabel = new Label(currencyFormatter.format(totalLine));
            totalLabel.setAlignment(Pos.CENTER_RIGHT);
            totalLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHalignment(totalLabel, javafx.geometry.HPos.CENTER);
            row.add(totalLabel, 3, 0);

            row.setOnMouseClicked(event -> {
                if (isMainBox) {
                    // если до этого показывалось окно оплаты — начинаем новую частичную оплату
                    if (isPaymentBoxShown) {
                        isPaymentBoxShown = false;
                        partialDetails.clear();
                        refreshUI();
                    }
                    moveItemToPartial(d);
                } else {
                    moveItemToMain(d);
                }
            });

            box.getChildren().add(row);

            total = total.add(totalLine);
        }

        if (isMainBox) {
            mainTotalLabel.setText(currencyFormatter.format(total));
        } else {
            partialTotalLabel.setText(currencyFormatter.format(total));
            if (!details.isEmpty()) {
                total_check = total;
            }
        }
    }

    // ---------- ЛОГИКА ПЕРЕНОСА ПОЗИЦИЙ ----------

    private void moveItemToPartial(OrderDetailResponse item) {
        int amountToMove = parseInputAmount();
        if (amountToMove <= 0)
            return;

        if (amountToMove > item.getAmount()) {
            amountToMove = item.getAmount();
        }

        // группируем по name + unitPrice
        OrderDetailResponse existing = partialDetails.stream()
                .filter(p -> Objects.equals(p.getName(), item.getName()))
                .filter(p -> p.getUnitPrice().compareTo(item.getUnitPrice()) == 0)
                .findFirst()
                .orElse(null);

        if (amountToMove == item.getAmount()) {
            // 🔹 Переносим всю строку из main в partial

            // 1) уменьшаем "оригинальный" detail в main до 0, чтобы его потом обновить в БД
            item.setAmount(0); // останется в orderDetails, но не будет показан (renderDetails его скроет)

            // 2) создаём НОВУЮ оплачиваемую строку в partial (id = null)
            if (existing != null && existing.getId() == null) {
                // уже есть такой новый кусок — просто увеличиваем его количество
                existing.setAmount(existing.getAmount() + amountToMove);
            } else {
                OrderDetailResponse paid = copyResponse(item);
                paid.setId(null);                // новый detail в БД
                paid.setAmount(amountToMove);
                paid.setStatus("PAID");
                partialDetails.add(paid);
            }

        } else {
            // 🔹 Переносим ЧАСТЬ строки
            item.setAmount(item.getAmount() - amountToMove);

            if (existing != null && existing.getId() == null) {
                existing.setAmount(existing.getAmount() + amountToMove);
            } else {
                OrderDetailResponse uiCopy = copyResponse(item);
                uiCopy.setId(null);              // новый detail
                uiCopy.setAmount(amountToMove);
                uiCopy.setStatus("PAID");
                partialDetails.add(uiCopy);
            }
        }

        displayField.clear();
        refreshUI();
    }

    private void moveItemToMain(OrderDetailResponse item) {
        int amountToMove = parseInputAmount();
        if (amountToMove <= 0)
            return;

        if (amountToMove > item.getAmount()) {
            amountToMove = item.getAmount();
        }

        OrderDetailResponse existing = orderDetails.stream()
                .filter(p -> Objects.equals(p.getName(), item.getName()))
                .filter(p -> p.getUnitPrice().compareTo(item.getUnitPrice()) == 0)
                .findFirst()
                .orElse(null);

        if (amountToMove == item.getAmount()) {
            // полностью возвращаем строку в основной бокс (до оплаты)
            if (existing != null && existing.getAmount() == 0) {
                // строка уже есть в main с amount 0 → просто увеличиваем
                existing.setAmount(existing.getAmount() + item.getAmount());
            } else if (existing != null && existing.getId() == null && item.getId() == null) {
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
                OrderDetailResponse newItem = copyResponse(item);
                newItem.setAmount(amountToMove);
                orderDetails.add(newItem);
            }
        }

        displayField.clear();
        refreshUI();
    }

    // ---------- ОПЛАТА ----------

    @FXML
    private void handlePayNoReceipt() {
        processPayment(false);
    }

    @FXML
    private void handlePayWithReceipt() {
        processPayment(true);
    }

    /**
     * Общая точка входа: и без чека, и с чеком.
     * Если order == null → создаём новый заказ, сохраняем детали и печатаем чек по нему.
     */
    private void processPayment(boolean withReceipt) {

        if (partialDetails.isEmpty()) {
            showError("Por favor, selecciona productos para cobrar");
            return;
        }
        if (selectedPaymentMethod.isEmpty()) {
            showError("Por favor, selecciona un método de pago.");
            return;
        }

        try {
            input = new BigDecimal(displayField.getText().isEmpty() ? "0" : displayField.getText())
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            input = BigDecimal.ZERO;
        }

        if (selectedPaymentMethod.equals("CASH") && input.compareTo(total_check) < 0) {
            showError("Importe no puede ser menor que la cuenta.");
            return;
        }

        PaymentInfo paymentInfo = computePaymentInfo();
        this.lastPaymentInfo = paymentInfo;

        try {
            if (order == null) {
                // 🔹 НОВЫЙ ЗАКАЗ: создаём, все partialDetails — это новые позиции
                order = OrderService.createOrder(new OrderRequest("PAID", null));

                List<OrderDetailRequest> createReqs = new ArrayList<>();
                for (OrderDetailResponse item : partialDetails) {
                    OrderDetailRequest req = new OrderDetailRequest();
                    req.setProductId(item.getProductId());
                    req.setOrderId(order.getId());
                    req.setName(item.getName());
                    req.setComment(item.getComment());
                    req.setAmount(item.getAmount());
                    req.setUnitPrice(item.getUnitPrice());
                    req.setStatus("PAID");
                    req.setBatchId(item.getBatchId());
                    req.setPaymentMethod(selectedPaymentMethod);
                    createReqs.add(req);
                }

                if (!createReqs.isEmpty()) {
                    List<OrderDetailResponse> created = OrderDetailService.createOrderDetailList(createReqs);
                    // заменяем UI-объекты на те, что вернул бэкенд (с id)
                    partialDetails.clear();
                    partialDetails.addAll(created);
                }

            } else {
                // 🔹 СУЩЕСТВУЮЩИЙ ЗАКАЗ: обновляем детали и создаём новые исходя из текущего UI
                persistPartialDetails();
            }

            if (withReceipt) {
                generateReceiptPdf(order, partialDetails, paymentInfo);
            }

            partialDetails.clear();
            refreshUI();
            showPaymentBox(paymentInfo);
            displayField.clear();

            anyPaymentDone = true;
            if (orderDetails.stream().noneMatch(od -> od.getAmount() > 0)) {
                Stage stage = (Stage) tableNameLabel.getScene().getWindow();
                stage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al procesar el pago: " + e.getMessage());
        }
    }

    private PaymentInfo computePaymentInfo() {
        // карта: просто total, без получено/сдачи
        if (selectedPaymentMethod.equals("CARD")) {
            return new PaymentInfo(
                    total_check.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            );
        }

        // наличка
        BigDecimal total = total_check.setScale(2, RoundingMode.HALF_UP);
        BigDecimal received = input.setScale(2, RoundingMode.HALF_UP);
        BigDecimal change = received.subtract(total).setScale(2, RoundingMode.HALF_UP);

        return new PaymentInfo(total, received, change);
    }

    private void generateReceiptPdf(OrderResponse order, List<OrderDetailResponse> paidDetails, PaymentInfo paymentInfo) {
        try {
            String fileName = "receipt-order-" + order.getId() + "-" + System.currentTimeMillis() + ".pdf";
            Path outputPath = Path.of(fileName);

            // table — это поле контроллера (заполняется в loadData)
            receiptFxToPdfService.createReceiptPdf(outputPath, order, paidDetails, table, paymentInfo);

            System.out.println("Receipt saved to: " + outputPath.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al generar el recibo: " + e.getMessage());
        }
    }

    // ---------- КАЛЬКУЛЯТОР / UI ВСПОМОГАТЕЛЬНОЕ ----------

    private void handleButtonClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();

        switch (buttonText) {
            case "C" -> displayField.clear();
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
            List<OrderDetailRequest> reqsToCreate = new ArrayList<>();
            for (OrderDetailResponse od : orderDetails) {
                if (od.getId() == null) {
                    continue;
                }

                idsToUpdate.add(od.getId());

                OrderDetailRequest req = new OrderDetailRequest(
                        od.getProductId(),
                        od.getOrderId(),
                        od.getName(),
                        od.getComment(),
                        od.getAmount(),
                        od.getUnitPrice(),
                        od.getStatus(),
                        od.getPaymentMethod(),
                        od.getBatchId(),
                        od.getCreatedAt(),
                        StartController.getCashSession().getId()

                );

                reqsToUpdate.add(req);
            }

            for (OrderDetailResponse pd : partialDetails) {
                Long productId = pd.getProductId();
                Long orderId   = pd.getOrderId() != null
                        ? pd.getOrderId()
                        : (order != null ? order.getId() : null);

                if (productId == null || orderId == null) {
                    throw new IllegalStateException("productId/orderId must not be null for partial payment detail");
                }

                if (pd.getId() != null) {
                    idsToUpdate.add(pd.getId());

                    OrderDetailRequest req = new OrderDetailRequest(
                            productId,
                            orderId,
                            pd.getName(),
                            pd.getComment(),
                            pd.getAmount(),
                            pd.getUnitPrice(),
                            "PAID",
                            selectedPaymentMethod,
                            pd.getBatchId(),
                            pd.getCreatedAt(),
                            StartController.getCashSession().getId()
                    );

                    reqsToUpdate.add(req);
                } else {
                    OrderDetailRequest createReq = new OrderDetailRequest(
                            productId,
                            orderId,
                            pd.getName(),
                            pd.getComment(),
                            pd.getAmount(),
                            pd.getUnitPrice(),
                            "PAID",
                            selectedPaymentMethod,
                            pd.getBatchId(),
                            pd.getCreatedAt(),
                            StartController.getCashSession().getId()
                    );
                    reqsToCreate.add(createReq);
                }
            }
            if (!idsToUpdate.isEmpty()) {
                OrderDetailService.updateOrderDetailList(idsToUpdate, reqsToUpdate);
            }
            if (!reqsToCreate.isEmpty()) {
                OrderDetailService.createOrderDetailList(reqsToCreate);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al guardar el pago parcial: " + e.getMessage());
        }
    }


    private int parseInputAmount() {
        String inputStr = displayField.getText().trim();
        if (inputStr.isEmpty())
            return 1;
        try {
            return Integer.parseInt(inputStr);
        } catch (NumberFormatException e) {
            displayField.clear();
            return -1;
        }
    }

    private OrderDetailResponse copyResponse(OrderDetailResponse src) {
        OrderDetailResponse copy = new OrderDetailResponse();
        copy.setId(src.getId());
        copy.setProductId(src.getProductId());
        copy.setName(src.getName());
        copy.setAmount(src.getAmount());
        copy.setUnitPrice(src.getUnitPrice());
        copy.setStatus(src.getStatus());
        copy.setComment(src.getComment());
        copy.setOrderId(src.getOrderId());
        copy.setBatchId(src.getBatchId());
        return copy;
    }

    private void showPaymentBox(PaymentInfo paymentInfo) {
        partialOrderBox.getChildren().clear();

        BigDecimal total = paymentInfo.getTotal();
        BigDecimal recibido = paymentInfo.getReceived();
        BigDecimal cambio = paymentInfo.getChange();

        StackPane wrapper = new StackPane();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefSize(partialOrderBox.getWidth(), partialOrderBox.getHeight() * 0.5);

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

        HBox rowTotal = createPaymentRow("COBRADO:", currencyFormatter.format(total), "#000");
        HBox rowRecibido = createPaymentRow("RECIBIDO:", currencyFormatter.format(recibido), "#000");
        HBox rowCambio = createPaymentRow("CAMBIO:", currencyFormatter.format(cambio), "#16a34a");

        if (recibido.compareTo(BigDecimal.ZERO) > 0) {
            paymentVB.getChildren().addAll(title, separator, rowTotal, rowRecibido, rowCambio);
        } else {
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

    private void selectPayment(String method) {
        cardButton.getStyleClass().removeAll("selected-card");
        cashButton.getStyleClass().removeAll("selected-cash");

        if (method.equals("card")) {
            cardButton.getStyleClass().add("selected-card");
            selectedPaymentMethod = "CARD";
        } else {
            cashButton.getStyleClass().add("selected-cash");
            selectedPaymentMethod = "CASH";
        }
    }

    public boolean anyPaymentDone() {
        return anyPaymentDone;
    }

    public PaymentInfo getLastPaymentInfo() {
        return lastPaymentInfo;
    }

    // --------- DTO для оплаты ---------
    public static class PaymentInfo {
        private final BigDecimal total;
        private final BigDecimal received;
        private final BigDecimal change;

        public PaymentInfo(BigDecimal total, BigDecimal received, BigDecimal change) {
            this.total = total;
            this.received = received;
            this.change = change;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public BigDecimal getReceived() {
            return received;
        }

        public BigDecimal getChange() {
            return change;
        }
    }
}
