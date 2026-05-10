package com.yebur.controller;

import com.yebur.app.App;
import com.yebur.model.response.CashSessionResponse;
import com.yebur.model.response.MonthlySummaryResponse;
import com.yebur.service.AnalyticsService;
import com.yebur.service.CashCountService;
import com.yebur.service.CashSessionService;
import com.yebur.ui.CustomDialog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class StartController {

    @FXML private VBox root;
    @FXML private VBox statsSection;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Label monthLabel;
    @FXML private Label revenueLabel;
    @FXML private Label orderCountLabel;

    private Region dimPane;
    private StackPane modalHost;
    @Getter private static CashSessionResponse cashSession;

    private YearMonth selectedMonth = YearMonth.now();
    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));

    public void setOverlay(Region dimPane, StackPane modalHost) {
        this.dimPane = dimPane;
        this.modalHost = modalHost;
    }

    @FXML
    public void initialize() {
        root.getStylesheets().add(
                getClass().getResource("/com/yebur/portal/views/data.css").toExternalForm()
        );
        prevMonthBtn.setTooltip(new javafx.scene.control.Tooltip("Mes anterior"));
        prevMonthBtn.setAccessibleText("Mes anterior");
        nextMonthBtn.setTooltip(new javafx.scene.control.Tooltip("Mes siguiente"));
        nextMonthBtn.setAccessibleText("Mes siguiente");
        nextMonthBtn.setDisable(true);
        updateMonthLabel();
        loadMonthStats();
    }

    @FXML
    public void openPOS(MouseEvent mouseEvent) {
        if (dimPane == null || modalHost == null) {
            CustomDialog.showError("Overlay no está inicializado (dimPane/modalHost).");
            return;
        }

        try {
            this.cashSession = CashSessionService.findCashSessionByStatus("OPEN");

            if (cashSession != null) {
                // ✅ Показать модалку "Turno ya está abierto" с данными
                int shiftNo = cashSession.getShiftNo();

                String openedAtText = formatOpenedAt(cashSession.getOpenedAt());

                CustomDialog.showCashSessionAlreadyOpenInPlace(
                        modalHost,
                        dimPane,
                        shiftNo,
                        openedAtText,
                        openTpv -> {
                            if (openTpv) openPosWindow();
                        }
                );
                return;
            }

            // если null (на всякий) — считаем что нет открытого
            showOpenNewTurnModal();

        } catch (Exception e) {
            // нет открытого turno / ошибка поиска -> предлагаем открыть новый
            showOpenNewTurnModal();
        }
    }

    private void showOpenNewTurnModal() {
        CustomDialog.confirmOpenCashSessionModernInPlace(
                modalHost,
                dimPane,
                "No hay ningún turno abierto",
                "Para comenzar a registrar ventas y operaciones, es necesario iniciar un nuevo período de trabajo.",
                "¿Quieres abrir un nuevo turno ahora?",
                ok -> {
                    if (!ok) return;

                    try {
                        if (hasAnyPersistedCashCount()) {
                            openShiftAndPos();
                        } else {
                            CustomDialog.showCashCountPromptInPlace(modalHost, dimPane, registerArqueo -> {
                                if (registerArqueo) {
                                    openInitialCashCountModalAndThenOpenShift();
                                } else {
                                    try {
                                        openShiftAndPos();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        CustomDialog.showError("No se pudo abrir el turno. Inténtalo de nuevo.");
                                    }
                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        CustomDialog.showError("No se pudo abrir el turno. Inténtalo de nuevo.");
                    }
                }
        );
    }

    private boolean hasAnyPersistedCashCount() throws Exception {
        return !CashCountService.getAllCashCounts().isEmpty();
    }

    private void openInitialCashCountModalAndThenOpenShift() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/cashCountModel.fxml")
            );
            Parent ccRoot = loader.load();
            Scene scene = new Scene(ccRoot);
            scene.getStylesheets().add(
                    getClass().getResource("/com/yebur/portal/views/cashCountModel.css").toExternalForm()
            );

            CashCountModelController ctrl = loader.getController();
            ctrl.preloadFromTotal(java.math.BigDecimal.ZERO);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(root.getScene().getWindow());
            stage.setResizable(false);
            stage.setScene(scene);

            stage.showAndWait();
            if (ctrl.isAccepted()) {
                openShiftAndPos();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            CustomDialog.showError("No se pudo registrar el arqueo inicial.");
        }
    }

    private void openShiftAndPos() throws Exception {
        this.cashSession = CashSessionService.openCashSession();
        openPosWindow();
    }

    private String formatOpenedAt(Object openedAt) {
        // Если openedAt у тебя LocalDateTime — форматируем красиво
        try {
            if (openedAt instanceof LocalDateTime dt) {
                return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
        } catch (Exception ignored) {}

        // Иначе оставляем как есть (например строка/Instant и т.д.)
        return String.valueOf(openedAt);
    }

    private void openPosWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/yebur/pos/pos.fxml"));
            Parent posRoot = loader.load();

            Scene scene = new Scene(posRoot);
            scene.getStylesheets().add(
                    App.class.getResource("/com/yebur/pos/pos.css").toExternalForm()
            );

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Orderly POS");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/yebur/icons/icon.png")));
            stage.setScene(scene);

            Stage currentStage = (Stage) root.getScene().getWindow();
            currentStage.hide();
            stage.showAndWait();
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("No se pudo abrir el POS.");
        }
    }

    @FXML
    private void reloadStats() {
        loadMonthStats();
    }

    @FXML
    private void prevMonth() {
        selectedMonth = selectedMonth.minusMonths(1);
        nextMonthBtn.setDisable(false);
        updateMonthLabel();
        loadMonthStats();
    }

    @FXML
    private void nextMonth() {
        selectedMonth = selectedMonth.plusMonths(1);
        if (selectedMonth.equals(YearMonth.now())) {
            nextMonthBtn.setDisable(true);
        }
        updateMonthLabel();
        loadMonthStats();
    }

    private void updateMonthLabel() {
        String raw = selectedMonth.format(MONTH_FMT);
        monthLabel.setText(Character.toUpperCase(raw.charAt(0)) + raw.substring(1));
    }

    private void loadMonthStats() {
        revenueLabel.setText("\u2014");
        revenueLabel.setStyle("");
        orderCountLabel.setText("\u2014");
        int year = selectedMonth.getYear();
        int month = selectedMonth.getMonthValue();
        new Thread(() -> {
            try {
                MonthlySummaryResponse data = AnalyticsService.getMonthlySummary(year, month);
                Platform.runLater(() -> {
                    revenueLabel.setText(moneyFmt.format(data.getTotalRevenue()) + " €");
                    orderCountLabel.setText(String.valueOf(data.getOrderCount()));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    revenueLabel.setText("Error al cargar los datos. Comprueba la conexión.");
                    revenueLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
                    orderCountLabel.setText("");
                });
            }
        }).start();
    }


}
