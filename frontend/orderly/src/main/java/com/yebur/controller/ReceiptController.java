package com.yebur.controller;

import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.RestTableResponse;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReceiptController {

    @FXML private VBox root;

    @FXML private ImageView imgLogo;

    @FXML private Label lblShopName;
    @FXML private Label lblAddress1;
    @FXML private Label lblAddress2;
    @FXML private Label lblPhone;
    @FXML private Label lblCif;

    @FXML private Label lblInvoice;
    @FXML private Label lblDate;

    @FXML private VBox itemsBox;

    @FXML private Label lblTaxBase;
    @FXML private Label lblTaxPercent;
    @FXML private Label lblTaxAmount;

    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblFooter;

    private final NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ---- ширина под рулон 80 мм ----
    private static final double RECEIPT_WIDTH   = 300; // px
    private static final double COL_QTY_WIDTH   = 40;  // UNID.
    private static final double COL_NAME_WIDTH  = 130; // DESCRIPCION
    private static final double COL_PRICE_WIDTH = 50;  // PRECIO
    private static final double COL_TOTAL_WIDTH = 60;  // IMPORTE

    // Данные бара «El Diván»
    private static final String SHOP_NAME  = "CAFETERIA EL DIVAN";
    private static final String SHOP_ADDR1 = "AVDA. REI EN JAUME I N.2";
    private static final String SHOP_ADDR2 = "46470 CATARROJA VALENCIA";
    private static final String SHOP_PHONE = "TEL 963127353";
    private static final String SHOP_CIF   = "CAFETERIA EL DIVAN S.L. CIF: B97340376";

    // 10% IVA, как в оригинальном чеке
    private static final BigDecimal IVA_RATE = new BigDecimal("0.10");

    @FXML
    private void initialize() {
        // фиксируем ширину
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

        // лого
        try (InputStream is =
                     getClass().getResourceAsStream("/com/yebur/icons/eldivan_logo.png")) { // поменяй путь!
            if (is != null) {
                imgLogo.setImage(new Image(is));
            }
        } catch (Exception ignored) {
        }

        // шапка бара
        lblShopName.setText(SHOP_NAME);
        lblAddress1.setText(SHOP_ADDR1);
        lblAddress2.setText(SHOP_ADDR2);
        lblPhone.setText(SHOP_PHONE);
        lblCif.setText(SHOP_CIF);

        lblFooter.setText("GRACIAS POR SU VISITA");
    }

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

        // FRA / FECHA (как в примере чека)
        String fra = "FRA SIN: COMPROBANTE";
        if (order != null && order.getId() != null) {
            fra = "FRA: " + order.getId();
        }
        lblInvoice.setText(fra);

        if (order != null && order.getDatetime() != null) {
            lblDate.setText("FECHA: " + order.getDatetime().format(DATE_TIME_FMT));
        } else {
            lblDate.setText("FECHA: N/A");
        }

        // Позиции
        itemsBox.getChildren().clear();
        BigDecimal total = BigDecimal.ZERO;

        if (details != null) {
            for (OrderDetailResponse d : details) {
                GridPane row = new GridPane();
                row.setHgap(4);
                row.setAlignment(Pos.CENTER_LEFT);

                if (row.getColumnConstraints().isEmpty()) {
                    ColumnConstraints colQty = new ColumnConstraints();
                    colQty.setMinWidth(COL_QTY_WIDTH);
                    colQty.setPrefWidth(COL_QTY_WIDTH);
                    colQty.setMaxWidth(COL_QTY_WIDTH);
                    colQty.setHalignment(HPos.CENTER);

                    ColumnConstraints colName = new ColumnConstraints();
                    colName.setMinWidth(COL_NAME_WIDTH);
                    colName.setPrefWidth(COL_NAME_WIDTH);
                    colName.setMaxWidth(COL_NAME_WIDTH);
                    colName.setHgrow(Priority.NEVER);

                    ColumnConstraints colPrice = new ColumnConstraints();
                    colPrice.setMinWidth(COL_PRICE_WIDTH);
                    colPrice.setPrefWidth(COL_PRICE_WIDTH);
                    colPrice.setMaxWidth(COL_PRICE_WIDTH);
                    colPrice.setHalignment(HPos.RIGHT);

                    ColumnConstraints colTotal = new ColumnConstraints();
                    colTotal.setMinWidth(COL_TOTAL_WIDTH);
                    colTotal.setPrefWidth(COL_TOTAL_WIDTH);
                    colTotal.setMaxWidth(COL_TOTAL_WIDTH);
                    colTotal.setHalignment(HPos.RIGHT);

                    row.getColumnConstraints().addAll(colQty, colName, colPrice, colTotal);
                }

                String name = d.getName() != null
                        ? d.getName()
                        : ("Producto #" + d.getProductId());

                Label qtyLbl = new Label(String.valueOf(d.getAmount()));
                qtyLbl.setAlignment(Pos.CENTER);
                qtyLbl.setMinWidth(COL_QTY_WIDTH);
                qtyLbl.setPrefWidth(COL_QTY_WIDTH);
                qtyLbl.setMaxWidth(COL_QTY_WIDTH);

                Label nameLbl = new Label(name);
                nameLbl.setWrapText(true);
                nameLbl.setMaxWidth(COL_NAME_WIDTH);
                GridPane.setHgrow(nameLbl, Priority.NEVER);

                BigDecimal unitPrice = d.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
                Label priceLbl = new Label(currencyFormatter.format(unitPrice));
                priceLbl.setMinWidth(COL_PRICE_WIDTH);
                priceLbl.setPrefWidth(COL_PRICE_WIDTH);
                priceLbl.setMaxWidth(COL_PRICE_WIDTH);

                BigDecimal lineTotal = unitPrice
                        .multiply(BigDecimal.valueOf(d.getAmount()))
                        .setScale(2, RoundingMode.HALF_UP);

                Label totalLbl = new Label(currencyFormatter.format(lineTotal));
                totalLbl.setMinWidth(COL_TOTAL_WIDTH);
                totalLbl.setPrefWidth(COL_TOTAL_WIDTH);
                totalLbl.setMaxWidth(COL_TOTAL_WIDTH);

                row.add(qtyLbl,   0, 0);
                row.add(nameLbl,  1, 0);
                row.add(priceLbl, 2, 0);
                row.add(totalLbl, 3, 0);

                itemsBox.getChildren().add(row);

                total = total.add(lineTotal);
            }
        }

        total = total.setScale(2, RoundingMode.HALF_UP);

        // НДС (BASE, %IVA, IMP.IVA)
        BigDecimal base = total
                .divide(BigDecimal.ONE.add(IVA_RATE), 2, RoundingMode.HALF_UP);
        BigDecimal ivaAmount = total.subtract(base);

        lblTaxBase.setText(currencyFormatter.format(base));
        lblTaxPercent.setText(IVA_RATE.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP) + " %");
        lblTaxAmount.setText(currencyFormatter.format(ivaAmount));

        // ИТОГИ
        lblTotal.setText("TOTAL EUROS  " + currencyFormatter.format(total));

        if (paymentInfo != null && paymentInfo.getReceived()
                .compareTo(BigDecimal.ZERO) > 0) {
            // уже оплачен
            lblPending.setText("PAGADO: " + currencyFormatter.format(paymentInfo.getReceived())
                    + "  CAMBIO: " + currencyFormatter.format(paymentInfo.getChange()));
            lblFooter.setText("GRACIAS POR SU VISITA");
        } else {
            // ещё не оплачен, как на фото
            lblPending.setText("PENDIENTE DE COBRO  " + currencyFormatter.format(total));
            lblFooter.setText("GRACIAS POR SU VISITA");
        }
    }
}
