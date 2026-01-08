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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StartController {

    @FXML private StackPane rootStack; // корень
    @FXML private VBox root;           // основной контент
    @FXML private Region dimPane;         // затемнение
    @FXML private StackPane modalHost;  // модалка

    @FXML
    public void initialize() {
        root.getStylesheets().add(
                getClass().getResource("/com/yebur/portal/views/data.css").toExternalForm()
        );

        // затемнение всегда на весь экран
        dimPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        modalHost.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        dimPane.prefWidthProperty().bind(rootStack.widthProperty());
        dimPane.prefHeightProperty().bind(rootStack.heightProperty());

        modalHost.prefWidthProperty().bind(rootStack.widthProperty());
        modalHost.prefHeightProperty().bind(rootStack.heightProperty());
    }

    /* ===================== OVERLAY ===================== */

    private void showOverlay() {
        dimPane.setVisible(true);
        modalHost.setVisible(true);

        dimPane.setMouseTransparent(false);
        modalHost.setMouseTransparent(false);
    }

    private void hideOverlay() {
        modalHost.getChildren().clear();
        dimPane.setVisible(false);
        modalHost.setVisible(false);
    }

    /* ===================== ACTION ===================== */

    @FXML
    public void openPOS(MouseEvent mouseEvent) {
        try {
            CashSessionResponse cashSession = CashSessionService.findCashSessionByStatus("OPEN");
            // Если есть открытый — открываем POS сразу
            if (cashSession != null) {
                openPosWindow();
            }
        } catch (Exception e) {
            // показываем in-place модалку
            CustomDialog.confirmOpenCashSessionModernInPlace(
                    modalHost,
                    dimPane,
                    "No hay ningún turno abierto",
                    "Para comenzar a registrar ventas y operaciones, es necesario iniciar un nuevo período de trabajo.",
                    "¿Quieres abrir un nuevo turno ahora?",
                    ok -> {
                        if (!ok) return;

                        try {
                            CashSessionService.openCashSession();
                            openPosWindow();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            CustomDialog.showError("No se pudo abrir el turno. Inténtalo de nuevo.");
                        }
                    }
            );
        }
    }

    /* ===================== POS ===================== */

    private void openPosWindow() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/com/yebur/pos/pos.fxml"));
            Parent posRoot = loader.load();

            Scene scene = new Scene(posRoot);
            scene.getStylesheets().add(
                    App.class.getResource("/com/yebur/pos/pos.css").toExternalForm()
            );

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Orderly POS");
            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/com/yebur/icons/icon.png"))
            );
            stage.setScene(scene);

            Stage currentStage = (Stage) rootStack.getScene().getWindow();
            currentStage.hide();
            stage.showAndWait();
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("No se pudo abrir el POS.");
        }
    }
}
