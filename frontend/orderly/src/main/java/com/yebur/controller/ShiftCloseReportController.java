package com.yebur.controller;

import com.yebur.model.response.CashOperationResponse;
import com.yebur.model.response.CashSessionResponse;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShiftCloseReportController {

    @FXML private Label dateLabel;
    @FXML private Label shiftNoLabel;
    @FXML private Label openedAtLabel;
    @FXML private Label closedAtLabel;
    @FXML private Label cashStartLabel;
    @FXML private Label totalSalesCashLabel;
    @FXML private Label totalSalesCardLabel;
    @FXML private Label differenceLabel;
    @FXML private VBox operationsVBox;
    @FXML private Button closeBtn;
    @FXML private Button printBtn;
    @FXML private Button emailBtn;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");

    public void populate(CashSessionResponse session, List<CashOperationResponse> ops) {
        dateLabel.setText(session.getBusinessDate().format(dateFmt));
        shiftNoLabel.setText(String.valueOf(session.getShiftNo()));
        openedAtLabel.setText(session.getOpenedAt().format(timeFmt));
        closedAtLabel.setText(session.getClosedAt().format(timeFmt));
        cashStartLabel.setText(moneyFmt.format(session.getCashStart()) + " €");
        totalSalesCashLabel.setText(moneyFmt.format(session.getTotalSalesCash()) + " €");
        totalSalesCardLabel.setText(moneyFmt.format(session.getTotalSalesCard()) + " €");
        differenceLabel.setText(moneyFmt.format(session.getDifference()) + " €");

        BigDecimal diff = session.getDifference();
        differenceLabel.setStyle("-fx-text-fill: " + (diff.compareTo(BigDecimal.ZERO) >= 0 ? "#16a34a" : "#dc2626") + ";");

        if (ops == null || ops.isEmpty()) {
            Label emptyLabel = new Label("Sin operaciones en este turno");
            emptyLabel.getStyleClass().add("grid-name");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            emptyLabel.setPadding(new javafx.geometry.Insets(20, 0, 12, 0));
            operationsVBox.getChildren().add(emptyLabel);
        } else {
            for (CashOperationResponse op : ops) {
                operationsVBox.getChildren().add(buildOperationRow(op));
            }
        }
    }

    private HBox buildOperationRow(CashOperationResponse op) {
        HBox row = new HBox(10);
        row.getStyleClass().add("row-line");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        Label horaLabel = new Label(op.getCreatedAt() != null ? op.getCreatedAt().format(timeFmt) : "--:--");
        horaLabel.getStyleClass().addAll("grid-name", "col-time");

        boolean isEntry = "ENTRADA".equals(op.getType());

        Label tipoLabel = new Label(isEntry ? "ENTRADA" : "SALIDA");
        tipoLabel.getStyleClass().addAll("grid-name", "col-type");
        tipoLabel.setStyle("-fx-text-fill: " + (isEntry ? "#16a34a" : "#dc2626") + ";");

        Label descLabel = new Label(op.getDescription());
        descLabel.getStyleClass().addAll("grid-name", "col-desc");
        HBox.setHgrow(descLabel, Priority.ALWAYS);

        Label importeLabel = new Label((isEntry ? "+" : "-") + moneyFmt.format(op.getAmount()) + " €");
        importeLabel.getStyleClass().addAll("grid-amount", "col-amount");
        importeLabel.setStyle("-fx-text-fill: " + (isEntry ? "#16a34a" : "#dc2626") + ";");

        row.getChildren().addAll(horaLabel, tipoLabel, descLabel, importeLabel);
        return row;
    }

    @FXML
    private void onClose() {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }

    @FXML
    private void onPrint() {
        // TODO: implement shift report printing (Phase 999.10)
    }

    @FXML
    private void onEmail() {
        // TODO: implement shift report send-by-email (Phase 999.10)
    }
}
