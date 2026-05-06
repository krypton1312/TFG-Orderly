package com.yebur.controller;

import com.yebur.model.request.CashSessionRequest;
import com.yebur.model.request.CashCountRequest;
import com.yebur.model.response.CashCountResponse;
import com.yebur.model.response.CashOperationResponse;
import com.yebur.model.response.CashSessionResponse;
import com.yebur.service.CashCountService;
import com.yebur.service.CashOperationService;
import com.yebur.service.CashSessionService;
import com.yebur.ui.CustomDialog;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Comparator;
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
    private CashCountResponse currentSessionCashCount;

    // ТЕПЕРЬ ЭТО НЕ "totals", а "текущие суммы"
    private BigDecimal cashAmount = BigDecimal.ZERO;
    private BigDecimal cardAmount = BigDecimal.ZERO;

    private Runnable onAfterClose;

    public void setOnAfterClose(Runnable callback) {
        this.onAfterClose = callback;
    }

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

        if (row == cashHBox) {
            if (singleClickTimer != null) singleClickTimer.stop();
            openCashCountModal();
            return;
        }

        BigDecimal value = parseInputToBigDecimal().setScale(2, RoundingMode.HALF_UP);

        // single click with timer
        if (singleClickTimer != null) singleClickTimer.stop();

        singleClickTimer = new PauseTransition(Duration.millis(220));
        singleClickTimer.setOnFinished(ev -> {
            if (row == cardHBox) {
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

            CashCountModelController efectivoCtrl = loader.getController();
            efectivoCtrl.setCurrentCashSession(cashSession);

            if (cashSession != null) {
                try {
                    currentSessionCashCount = CashCountService.getCashCountBySessionId(cashSession.getId());
                    efectivoCtrl.preload(currentSessionCashCount);
                } catch (Exception ignored) {
                    currentSessionCashCount = null;
                }
            }

            // Dim the portal background while the modal is open
            Scene ownerScene = cashRoot.getScene();
            Parent originalRoot = ownerScene.getRoot();
            StackPane dimWrapper = new StackPane(originalRoot);
            Region dimOverlay = new Region();
            dimOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
            dimOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dimWrapper.getChildren().add(dimOverlay);
            ownerScene.setRoot(dimWrapper);

            stage.setOnHidden(ev -> {
                dimWrapper.getChildren().remove(originalRoot);
                ownerScene.setRoot(originalRoot);

                if (!efectivoCtrl.isAccepted()) {
                    return;
                }

                this.cashCountController = efectivoCtrl;

                try {
                    if (cashSession != null) {
                        currentSessionCashCount = CashCountService.getCashCountBySessionId(cashSession.getId());
                    }

                    BigDecimal modalTotal = currentSessionCashCount != null
                            ? cashCountTotal(currentSessionCashCount)
                            : efectivoCtrl.getTotal();
                    if (modalTotal == null) modalTotal = BigDecimal.ZERO;

                    cashAmount = modalTotal;
                    updateCurrentSessionCashEndActual(modalTotal);
                    closeErrorLabel.setText("");
                    refreshAll();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    closeErrorLabel.setText("No se pudo actualizar el arqueo de efectivo.");
                }
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
            ctrl.setPersistCashCountOnAccept(false);

            CashCountResponse depositSource = findDepositSourceCashCount();
            if (depositSource != null) {
                ctrl.preload(depositSource);
            } else {
                ctrl.preloadFromTotal(cashSession != null ? cashSession.getCashStart() : BigDecimal.ZERO);
            }

            // Dim the portal background while the modal is open
            Scene ownerScene = cashRoot.getScene();
            Parent originalRoot = ownerScene.getRoot();
            StackPane dimWrapper = new StackPane(originalRoot);
            Region dimOverlay = new Region();
            dimOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
            dimOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dimWrapper.getChildren().add(dimOverlay);
            ownerScene.setRoot(dimWrapper);

            stage.setOnHidden(ev -> {
                // Must remove originalRoot from dimWrapper before restoring it as scene root;
                // otherwise JavaFX throws "already inside a scene-graph" exception.
                dimWrapper.getChildren().remove(originalRoot);
                ownerScene.setRoot(originalRoot);

                if (ctrl.isAccepted()) {
                    try {
                        syncDepositWithPreviousShift(ctrl, depositSource);
                        updateCurrentSessionCashStart(ctrl.getTotal());
                        cashStartLabel.setText(currencyFormatter.format(cashSession.getCashStart()));
                        closeErrorLabel.setText("");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        closeErrorLabel.setText("No se pudo actualizar el depósito entre turnos.");
                    }
                }
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
        CustomDialog.confirmGuardarArqueoInScene(
                cashRoot.getScene(),
                this::doCloseTurn,
                () -> {}
        );
    }

    private void doCloseTurn() {
        try {
            CashCountRequest req;
            if (cashCountController != null) {
                req = cashCountController.buildCashCountRequest();
            } else {
                req = new CashCountRequest();
            }

            CashSessionResponse closed = CashSessionService.closeCashSession(cashSession.getId(), req);

            List<CashOperationResponse> ops =
                    CashOperationService.getRealCashOperationsBySessionId(closed.getId());

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
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initOwner(cashRoot.getScene().getWindow());
            stage.setScene(scene);

            // Dim the background while the report is shown
            Scene ownerScene = cashRoot.getScene();
            Parent originalRoot = ownerScene.getRoot();
            StackPane dimWrapper = new StackPane(originalRoot);
            Region dimOverlay = new Region();
            dimOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
            dimOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dimWrapper.getChildren().add(dimOverlay);
            ownerScene.setRoot(dimWrapper);

            stage.showAndWait();

            dimWrapper.getChildren().remove(originalRoot);
            ownerScene.setRoot(originalRoot);

            if (onAfterClose != null) {
                onAfterClose.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
            closeErrorLabel.setText("Error al cerrar el turno. Inténtelo de nuevo.");
        }
    }

    private BigDecimal cashCountTotal(CashCountResponse cc) {
        if (cc == null) return BigDecimal.ZERO;
        BigDecimal t = BigDecimal.ZERO;
        t = t.add(denom("0.01", cc.getC001()));
        t = t.add(denom("0.02", cc.getC002()));
        t = t.add(denom("0.05", cc.getC005()));
        t = t.add(denom("0.10", cc.getC010()));
        t = t.add(denom("0.20", cc.getC020()));
        t = t.add(denom("0.50", cc.getC050()));
        t = t.add(denom("1.00", cc.getC100()));
        t = t.add(denom("2.00", cc.getC200()));
        t = t.add(denom("5.00", cc.getB005()));
        t = t.add(denom("10.00", cc.getB010()));
        t = t.add(denom("20.00", cc.getB020()));
        t = t.add(denom("50.00", cc.getB050()));
        t = t.add(denom("100.00", cc.getB100()));
        t = t.add(denom("200.00", cc.getB200()));
        t = t.add(denom("500.00", cc.getB500()));
        return t;
    }

    private BigDecimal denom(String value, Integer count) {
        return new BigDecimal(value).multiply(BigDecimal.valueOf(count != null ? count : 0));
    }

    private void updateCurrentSessionCashStart(BigDecimal cashStart) throws Exception {
        CashSessionRequest request = new CashSessionRequest();
        request.setClosedAt(cashSession.getClosedAt());
        request.setCashStart(cashStart);
        request.setCashEndExpected(cashSession.getCashEndExpected());
        request.setCashEndActual(cashSession.getCashEndActual());
        request.setDifference(cashSession.getDifference());
        request.setTotalSalesCash(cashSession.getTotalSalesCash());
        request.setTotalSalesCard(cashSession.getTotalSalesCard());
        request.setStatus(toBackendCashSessionStatus(cashSession.getStatus()));
        this.cashSession = CashSessionService.updateCashSession(cashSession.getId(), request);
    }

    private void updateCurrentSessionCashEndActual(BigDecimal cashEndActual) throws Exception {
        CashSessionRequest request = new CashSessionRequest();
        request.setClosedAt(cashSession.getClosedAt());
        request.setCashStart(cashSession.getCashStart());
        request.setCashEndExpected(cashSession.getCashEndExpected());
        request.setCashEndActual(cashEndActual);
        request.setDifference(cashSession.getDifference());
        request.setTotalSalesCash(cashSession.getTotalSalesCash());
        request.setTotalSalesCard(cashSession.getTotalSalesCard());
        request.setStatus(toBackendCashSessionStatus(cashSession.getStatus()));
        this.cashSession = CashSessionService.updateCashSession(cashSession.getId(), request);
    }

    private void syncDepositWithPreviousShift(CashCountModelController ctrl, CashCountResponse depositSource) throws Exception {
        CashCountRequest request = ctrl.buildCashCountRequest();

        if (depositSource != null && depositSource.getId() != null) {
            request.setSessionId(depositSource.getSession_id());

            try {
                CashCountService.updateCashCount(depositSource.getId(), request);
                return;
            } catch (Exception ignored) {
                if (depositSource.getSession_id() != null) {
                    CashCountService.createCashCount(request);
                    return;
                }
            }
        }

        CashSessionResponse previousClosed = findPreviousClosedShift();

        if (previousClosed != null) {
            request.setSessionId(previousClosed.getId());

            try {
                CashCountResponse existing = CashCountService.getCashCountBySessionId(previousClosed.getId());
                CashCountService.updateCashCount(existing.getId(), request);
            } catch (Exception ignored) {
                CashCountService.createCashCount(request);
            }
            return;
        }

        request.setSessionId(null);

        try {
            CashCountResponse existing = CashCountService.getLatestUnassignedCashCount();
            if (existing == null) {
                CashCountService.createCashCount(request);
                return;
            }
            CashCountService.updateCashCount(existing.getId(), request);
        } catch (Exception ignored) {
            CashCountService.createCashCount(request);
        }
    }

    private CashCountResponse findDepositSourceCashCount() throws Exception {
        CashSessionResponse previousClosed = findPreviousClosedShift();

        if (previousClosed != null) {
            try {
                return CashCountService.getCashCountBySessionId(previousClosed.getId());
            } catch (Exception ignored) {
                // Fall through to bootstrap cash count fallback
            }
        }

        return CashCountService.getLatestUnassignedCashCount();
    }

    private CashSessionResponse findPreviousClosedShift() throws Exception {
        return CashSessionService.getAllCashSessions().stream()
                .filter(session -> session != null
                        && session.getId() != null
                        && !session.getId().equals(cashSession.getId())
                        && isClosedStatus(session.getStatus())
                        && session.getClosedAt() != null)
                .filter(session -> cashSession.getOpenedAt() == null || !session.getClosedAt().isAfter(cashSession.getOpenedAt()))
                .max(Comparator.comparing(CashSessionResponse::getClosedAt))
                .orElse(null);
    }

    private boolean isClosedStatus(String status) {
        return "CLOSED".equalsIgnoreCase(status) || "Cerrado".equalsIgnoreCase(status);
    }

    private String toBackendCashSessionStatus(String status) {
        if (status == null || status.isBlank()) return "OPEN";
        if ("Abierto".equalsIgnoreCase(status)) return "OPEN";
        if ("Cerrado".equalsIgnoreCase(status)) return "CLOSED";
        return status.toUpperCase(Locale.ROOT);
    }

    private CashCountRequest buildCashCountRequestFromResponse(CashCountResponse cc) {
        CashCountRequest req = new CashCountRequest();
        req.setC001(cc.getC001());
        req.setC002(cc.getC002());
        req.setC005(cc.getC005());
        req.setC010(cc.getC010());
        req.setC020(cc.getC020());
        req.setC050(cc.getC050());
        req.setC100(cc.getC100());
        req.setC200(cc.getC200());
        req.setB005(cc.getB005());
        req.setB010(cc.getB010());
        req.setB020(cc.getB020());
        req.setB050(cc.getB050());
        req.setB100(cc.getB100());
        req.setB200(cc.getB200());
        req.setB500(cc.getB500());
        return req;
    }
}
