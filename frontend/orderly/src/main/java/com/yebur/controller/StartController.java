package com.yebur.controller;

import com.yebur.app.App;
import com.yebur.model.response.CashSessionResponse;
import com.yebur.service.CashSessionService;
import com.yebur.ui.CustomDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StartController {

    @FXML private VBox root;

    private Region dimPane;
    private StackPane modalHost;
    @Getter private static CashSessionResponse cashSession;

    public void setOverlay(Region dimPane, StackPane modalHost) {
        this.dimPane = dimPane;
        this.modalHost = modalHost;
    }

    @FXML
    public void initialize() {
        root.getStylesheets().add(
                getClass().getResource("/com/yebur/portal/views/data.css").toExternalForm()
        );
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
                        this.cashSession = CashSessionService.openCashSession();
                        openPosWindow();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        CustomDialog.showError("No se pudo abrir el turno. Inténtalo de nuevo.");
                    }
                }
        );
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


}
