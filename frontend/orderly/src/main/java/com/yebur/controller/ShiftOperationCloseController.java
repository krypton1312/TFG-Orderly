package com.yebur.controller;

import com.yebur.model.request.CashCountRequest;
import com.yebur.model.response.CashCountResponse;
import com.yebur.model.response.CashOperationResponse;
import com.yebur.model.response.CashSessionResponse;
import com.yebur.service.CashCountService;
import com.yebur.service.CashOperationService;
import com.yebur.service.CashSessionService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ShiftOperationCloseController {

    @FXML private Label cashTotalLabel;
    @FXML private VBox cashRoot;

    @FXML private Label dateValueLabel;
    @FXML private Label shiftValueLabel;
    @FXML private Label cashStartLabel;
    @FXML private Label displayLabel;
    @FXML private Label cashAmountLabel;
    @FXML private Label cardAmountLabel;

    @FXML private HBox cashHBox;
    @FXML private HBox cardHBox;

    @FXML private Button cerrarTurnoBtn;
    @FXML private Label closeErrorLabel;

    private PauseTransition singleClickTimer;

    private CashSessionResponse cashSession;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    // calculator input (no currency)
    private final StringBuilder input = new StringBuilder("0");

    private CashCountModelController cashCountController;

    // ТЕПЕРЬ ЭТО НЕ "totals", а "текущие суммы"
    private BigDecimal cashAmount = BigDecimal.ZERO;
    private BigDecimal cardAmount = BigDecimal.ZERO;

    @FXML
    public void initialize() {
        cashRoot.getStylesheets().add(
                getClass().getResource("/com/yebur/portal/views/shiftOperationClose.css").toExternalForm()
        );

        getLastCashSession();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (cashSession != null) {
            dateValueLabel.setText(cashSession.getBusinessDate().format(formatter));
            shiftValueLabel.setText(String.valueOf(cashSession.getShiftNo()));
            cashStartLabel.setText(currencyFormatter.format(cashSession.getCashStart()));
        }

        refreshAmountsLabels();
        refreshTotalLabel();
        updateDisplay();
    }

    private void getLastCashSession() {
        try {
            this.cashSession = CashSessionService.findCashSessionByStatus("OPEN");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /* =========================
       CALCULATOR HANDLERS
       ========================= */

    @FXML
    private void onDigit(javafx.event.ActionEvent e) {
        String d = ((Button) e.getSource()).getText();

        if (input.toString().equals("0") && !input.toString().contains(".")) {
            input.setLength(0);
            input.append(d);
        } else {
            if (input.length() < 12) {
                input.append(d);
            }
        }
        updateDisplay();
    }

    @FXML
    private void onDoubleZero() {
        if (input.toString().equals("0") && !input.toString().contains(".")) return;

        if (input.length() <= 10) input.append("00");
        updateDisplay();
    }

    @FXML
    private void onDot() {
        if (!input.toString().contains(".")) input.append(".");
        updateDisplay();
    }

    @FXML
    private void onBackspace() {
        if (input.length() <= 1) {
            input.setLength(0);
            input.append("0");
        } else {
            input.deleteCharAt(input.length() - 1);
            if (input.length() == 0 || input.toString().equals("-")) {
                input.setLength(0);
                input.append("0");
            }
        }
        updateDisplay();
    }

    @FXML
    private void onClear() {
        input.setLength(0);
        input.append("0");
        updateDisplay();
    }

    @FXML
    private void onEquals() {
        BigDecimal value = parseInputToBigDecimal().setScale(2, RoundingMode.HALF_UP);
        input.setLength(0);
        input.append(value.toPlainString());
        updateDisplay();
    }

    private BigDecimal parseInputToBigDecimal() {
        try {
            String s = input.toString();
            if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
            if (s.isBlank()) s = "0";
            return new BigDecimal(s);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private void updateDisplay() {
        BigDecimal value = parseInputToBigDecimal().setScale(2, RoundingMode.HALF_UP);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        displayLabel.setText(nf.format(value));
    }

    /* =========================
       LABELS REFRESH
       ========================= */

    private void refreshAmountsLabels() {
        cashAmountLabel.setText(currencyFormatter.format(cashAmount));
        cardAmountLabel.setText(currencyFormatter.format(cardAmount));
    }

    private void refreshTotalLabel() {
        cashTotalLabel.setText(currencyFormatter.format(cashAmount.add(cardAmount)));
    }

    private void refreshAll() {
        refreshAmountsLabels();
        refreshTotalLabel();
    }

    /* =========================
       CLICK HANDLER
       ========================= */

    @FXML
    private void onPayRowClick(MouseEvent e) {
        Node node = (Node) e.getTarget();
        while (node != null && !(node instanceof HBox)) node = node.getParent();
        if (!(node instanceof HBox row)) return;

        BigDecimal value = parseInputToBigDecimal().setScale(2, RoundingMode.HALF_UP);

        // double click: CASH -> open modal
        if (e.getClickCount() == 2) {
            if (singleClickTimer != null) singleClickTimer.stop();
            if (row == cashHBox) openCashCountModal();
            return;
        }

        // single click with timer
        if (singleClickTimer != null) singleClickTimer.stop();

        singleClickTimer = new PauseTransition(Duration.millis(220));
        singleClickTimer.setOnFinished(ev -> {
            if (row == cashHBox) {
                // УСТАНАВЛИВАЕМ сумму налички (если ввёл 0 -> будет вычитание/обнуление)
                cashAmount = value;
                refreshAll();
                onClear();

            } else if (row == cardHBox) {
                // УСТАНАВЛИВАЕМ сумму карты
                cardAmount = value;
                refreshAll();
                onClear();
            }
        });

        singleClickTimer.playFromStart();
    }

    private void openCashCountModal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/cashCountModel.fxml")
            );

            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/yebur/portal/views/cashCountModel.css").toExternalForm()
            );

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(cashRoot.getScene().getWindow());
            stage.setResizable(false);
            stage.setScene(scene);

            this.cashCountController = loader.getController();
            cashCountController.setCurrentCashSession(cashSession);

            stage.setOnHidden(ev -> {
                BigDecimal modalTotal = cashCountController != null ? cashCountController.getTotal() : null;
                if (modalTotal == null) modalTotal = BigDecimal.ZERO;

                // Модалка возвращает ИТОГ НАЛИЧКИ -> просто устанавливаем
                cashAmount = modalTotal;

                refreshAll();
            });

            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onModificarDeposito() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/cashCountModel.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/yebur/portal/views/cashCountModel.css").toExternalForm()
            );

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(cashRoot.getScene().getWindow());
            stage.setResizable(false);
            stage.setScene(scene);

            CashCountModelController ctrl = loader.getController();
            ctrl.setCurrentCashSession(cashSession);

            // Preload existing CashCount denominations if one exists for this session
            if (cashSession != null) {
                try {
                    CashCountResponse existing = CashCountService.getCashCountBySessionId(cashSession.getId());
                    ctrl.preload(existing);
                } catch (Exception ignored) {
                    // No CashCount yet — fields start at zero (default)
                }
            }

            stage.setOnHidden(ev -> {
                this.cashCountController = ctrl;
                BigDecimal modalTotal = ctrl.getTotal();
                if (modalTotal == null) modalTotal = BigDecimal.ZERO;
                cashAmount = modalTotal;
                refreshAll();
            });

            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onCerrarTurno() {
        if (cashSession == null) {
            closeErrorLabel.setText("No hay un turno abierto");
            return;
        }
        closeErrorLabel.setText("");
        try {
            CashCountRequest req = cashCountController != null
                    ? cashCountController.buildCashCountRequest()
                    : new CashCountRequest();

            CashSessionResponse closed = CashSessionService.closeCashSession(cashSession.getId(), req);

            List<CashOperationResponse> ops =
                    CashOperationService.getCashOperationsBySessionId(closed.getId());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/shiftCloseReport.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            ShiftCloseReportController reportCtrl = loader.getController();
            reportCtrl.populate(closed, ops);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(cashRoot.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            closeErrorLabel.setText("Error al cerrar el turno. Inténtelo de nuevo.");
        }
    }
}
