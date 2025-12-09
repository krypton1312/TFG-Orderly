package com.yebur.controller;

import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.RestTableResponse;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReceiptController {

    @FXML private VBox root;

    @FXML private Label lblShopName;
    @FXML private Label lblAddress;
    @FXML private Label lblPhone;

    @FXML private Label lblOrderInfo;
    @FXML private Label lblDate;
    @FXML private Label lblTable;
    @FXML private Label lblPayment;

    @FXML private VBox itemsBox;

    @FXML private Label lblTotal;
    @FXML private Label lblFooter;

    private final NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ---- Фиксированные размеры чека (под 58mm, можно подправить под свой принтер) ----
    private static final double RECEIPT_WIDTH    = 220;  // общая ширина чека, px
    private static final double COL_NAME_WIDTH   = 130;  // ширина колонки "Producto"
    private static final double COL_QTY_WIDTH    = 30;   // ширина колонки "Uds"
    private static final double COL_TOTAL_WIDTH  = 60;   // ширина колонки "Total"

    // Можно потом вынести в настройки
    private static final String SHOP_NAME  = "Orderly Bar";
    private static final String SHOP_ADDR  = "C/ Valencia 123, Valencia";
    private static final String SHOP_PHONE = "Tel: +34 600 000 000";

    public VBox getRoot() {
        return root;
    }

    /**
     * Заполняем чек данными.
     */
    public void setData(OrderResponse order,
                        List<OrderDetailResponse> details,
                        RestTableResponse table,
                        PartialPaymentController.PaymentInfo paymentInfo) {

        // ---- фиксируем ширину корня и контейнера с позициями ----
        if (root != null) {
            root.setMinWidth(RECEIPT_WIDTH);
            root.setPrefWidth(RECEIPT_WIDTH);
            root.setMaxWidth(RECEIPT_WIDTH);
        }
        if (itemsBox != null) {
            itemsBox.setMinWidth(RECEIPT_WIDTH);
            itemsBox.setPrefWidth(RECEIPT_WIDTH);
            itemsBox.setMaxWidth(RECEIPT_WIDTH);
        }

        // Шапка бара
        lblShopName.setText(SHOP_NAME);
        lblAddress.setText(SHOP_ADDR);
        lblPhone.setText(SHOP_PHONE);

        // Инфо по заказу
        if (order != null && order.getId() != null) {
            lblOrderInfo.setText("Pedido: " + order.getId());
        } else {
            lblOrderInfo.setText("Pedido: -");
        }

        if (order != null && order.getDatetime() != null) {
            lblDate.setText("Fecha: " + order.getDatetime().format(DATE_TIME_FMT));
        } else {
            lblDate.setText("Fecha: N/A");
        }

        if (table != null && table.getName() != null) {
            lblTable.setText("Mesa: " + table.getName());
        } else {
            lblTable.setText("Sin mesa");
        }

        String paymentMethod = "";
        if (details != null && !details.isEmpty()
                && details.get(0).getPaymentMethod() != null) {
            paymentMethod = details.get(0).getPaymentMethod();
        }

        if ("CASH".equals(paymentMethod)) {
            paymentMethod = "Efectivo";
        } else if ("CARD".equals(paymentMethod)) {
            paymentMethod = "Tarjeta";
        }

        lblPayment.setText("Metodo pago: " +
                (!paymentMethod.isEmpty() ? paymentMethod : "N/A"));

        // Позиции
        itemsBox.getChildren().clear();
        BigDecimal total = BigDecimal.ZERO;

        if (details != null) {
            for (OrderDetailResponse d : details) {
                GridPane row = new GridPane();
                row.setHgap(4);
                row.setAlignment(Pos.CENTER_LEFT);

                // Фиксированные колонки (px), а не проценты
                if (row.getColumnConstraints().isEmpty()) {
                    ColumnConstraints colName = new ColumnConstraints();
                    colName.setMinWidth(COL_NAME_WIDTH);
                    colName.setPrefWidth(COL_NAME_WIDTH);
                    colName.setMaxWidth(COL_NAME_WIDTH);
                    colName.setHgrow(Priority.NEVER);

                    ColumnConstraints colQty = new ColumnConstraints();
                    colQty.setMinWidth(COL_QTY_WIDTH);
                    colQty.setPrefWidth(COL_QTY_WIDTH);
                    colQty.setMaxWidth(COL_QTY_WIDTH);
                    colQty.setHalignment(HPos.CENTER);

                    ColumnConstraints colTotal = new ColumnConstraints();
                    colTotal.setMinWidth(COL_TOTAL_WIDTH);
                    colTotal.setPrefWidth(COL_TOTAL_WIDTH);
                    colTotal.setMaxWidth(COL_TOTAL_WIDTH);
                    colTotal.setHalignment(HPos.RIGHT);

                    row.getColumnConstraints().addAll(colName, colQty, colTotal);
                }

                String name = d.getName() != null
                        ? d.getName()
                        : ("Producto #" + d.getProductId());

                Label nameLbl = new Label(name);
                nameLbl.setWrapText(true);
                nameLbl.setMaxWidth(COL_NAME_WIDTH);
                GridPane.setHgrow(nameLbl, Priority.NEVER);

                Label qtyLbl = new Label("x" + d.getAmount());
                qtyLbl.setAlignment(Pos.CENTER);
                qtyLbl.setMinWidth(COL_QTY_WIDTH);
                qtyLbl.setPrefWidth(COL_QTY_WIDTH);
                qtyLbl.setMaxWidth(COL_QTY_WIDTH);

                BigDecimal lineTotal = d.getUnitPrice()
                        .multiply(BigDecimal.valueOf(d.getAmount()))
                        .setScale(2, RoundingMode.HALF_UP);

                Label totalLbl = new Label(currencyFormatter.format(lineTotal));
                totalLbl.setAlignment(Pos.CENTER_RIGHT);
                totalLbl.setMinWidth(COL_TOTAL_WIDTH);
                totalLbl.setPrefWidth(COL_TOTAL_WIDTH);
                totalLbl.setMaxWidth(COL_TOTAL_WIDTH);

                row.add(nameLbl, 0, 0);
                row.add(qtyLbl, 1, 0);
                row.add(totalLbl, 2, 0);

                itemsBox.getChildren().add(row);

                total = total.add(lineTotal);
            }
        }

        total = total.setScale(2, RoundingMode.HALF_UP);
        lblTotal.setText("TOTAL: " + currencyFormatter.format(total));

        if (paymentInfo != null && paymentInfo.getReceived().compareTo(BigDecimal.ZERO) > 0) {
            lblFooter.setText(
                    "Gracias!\n" +
                            "Pagado: " + currencyFormatter.format(paymentInfo.getReceived()) +
                            "\nCambio: " + currencyFormatter.format(paymentInfo.getChange())
            );
        } else {
            lblFooter.setText("Gracias!");
        }
    }
}
