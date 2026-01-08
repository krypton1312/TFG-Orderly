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

public class StartController {

    @FXML private VBox root;

    private Region dimPane;
    private StackPane modalHost;

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
        try {
            CashSessionResponse cashSession = CashSessionService.findCashSessionByStatus("OPEN");
            if (cashSession != null) {
                openPosWindow();
                return;
            }
        } catch (Exception e) {
            if (dimPane == null || modalHost == null) {
                CustomDialog.showError("Overlay no está inicializado (dimPane/modalHost).");
                return;
            }

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
